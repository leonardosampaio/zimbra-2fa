#!/bin/bash

if [ "$(whoami)" != "root" ]; then
        echo "Invalid user, run as root";
        exit;
fi

echo "Setting environment variables";

#######################################################################################

zimbraBinPath="/opt/zimbra/bin"
localconfig=`$zimbraBinPath/zmlocalconfig -s`
domain=`echo "$localconfig" | grep -Po '(?<=^zimbra_server_hostname = ).*$'`
ldapDomains=`$zimbraBinPath/zmprov gad`
zimbraPath=`echo "$localconfig" | grep -Po '(?<=^zimbra_home = ).*$'`
installFolder=$zimbraPath"/2fa"
jettyPath=$zimbraPath"/jetty"
mysqlZimbraDb=zimbra
mysqlRootUser=root
mysqlRootPassword=`echo "$localconfig" | grep -Po '(?<=^mysql_root_password = ).*$'`
jettyCommonLibDir=$jettyPath"/common/lib"

zimbraUser=zimbra
jettyUser=`stat -c '%U' $jettyCommonLibDir`
mysqlUser=`ps -ef | grep mysql.sock | grep -v grep | awk '{ print $1 }'`

#######################################################################################

if [ -z "$mysqlUser" ]
then
        echo "MySQL/MariaDB not running, exiting";
        exit;
fi

echo "Installing rsync"
apt install -y rsync

date=$(date '+%Y-%m-%d')
randomPassword=`date +%s | sha256sum | base64 | head -c 32 ; echo`

echo 'Creating install folder'
sudo mkdir -p $installFolder"/backup"
sudo chown -R $zimbraUser:$zimbraUser $installFolder

echo 'Creating database';
sed -ie "s/CHANGETHIS/$randomPassword/g" sql/create.sql
for script in sql/*.sql; do
        echo "Running $script"
        sudo -u $mysqlUser $zimbraBinPath"/mysql" -u $mysqlRootUser -p$mysqlRootPassword $mysqlZimbraDb < $script
done

echo 'Copying 2fa jar library';
sudo -u $jettyUser cp -R dist/2fa-1.0.jar $jettyCommonLibDir

echo 'Installing zimlets';
sudo -u $zimbraUser cp -R dist/br_com_sampaio_twofa_client.zip $zimbraPath"/zimlets"
sudo -u $zimbraUser cp -R dist/br_com_sampaio_twofa_admin.zip $zimbraPath"/zimlets"
sudo -u $zimbraUser $zimbraBinPath"/zmzimletctl" deploy $zimbraPath"/zimlets/br_com_sampaio_twofa_client.zip"
sudo -u $zimbraUser $zimbraBinPath"/zmzimletctl" deploy $zimbraPath"/zimlets/br_com_sampaio_twofa_admin.zip"

echo 'Modifing login page';
md5=`/usr/bin/md5sum $jettyPath/webapps/zimbra/public/login.jsp | awk '{ print $1 }'`
sudo -u $jettyUser cp -R $jettyPath"/webapps/zimbra/public/login.jsp" $installFolder"/backup/$date-$md5-login.jsp"
sudo -u $jettyUser cp -R jsp/login.8.8.15.2fa.jsp $jettyPath"/webapps/zimbra/public/login.jsp"

read -p "Enter the domains that will use 2fa (e.g., zimbra_ldap_domain.com:https_domain.com zimbra_ldap_domain_2.com:https_domain_2.com): " domains;
echo 'Copying 2fa configuration file';
#if you need to change this, define environment variable 2FA_CONFIG_FILE_PATH, visible to Jetty server
sudo -u $zimbraUser cp -R config/config.properties $installFolder"/config.properties"
sudo -u $zimbraUser /bin/sed -i "s/^mysqlPassword=.*/mysqlPassword=$randomPassword/" $installFolder"/config.properties"
sudo -u $zimbraUser /bin/sed -i "s/^domains=.*/domains=$domains/" $installFolder"/config.properties"
echo "Configuration file created at $installFolder/config.properties"

echo 'Activating jsp files in zimlets';
sudo -u $zimbraUser $zimbraBinPath"/zmprov" ms "$domain" zimbraZimletJspEnabled TRUE

echo 'Installing single app password extension';
sudo -u $jettyUser install -d $zimbraBinPath"/lib/ext/singlepassword/"
sudo -u $jettyUser unzip -o dist/zimbra-singlepassword-extension.zip
sudo -u $jettyUser rsync -rt -i --delete zimbra-singlepassword-extension/ $zimbraPath"/lib/ext/singlepassword/"

for ldapDomain in $ldapDomains; do
        echo "Activating single app password extension for $ldapDomain";
        sudo -u $zimbraUser $zimbraBinPath"/zmprov" modifyDomain "$ldapDomain" zimbraAuthMech custom:singlepassword
        #optional
        #sudo -u $zimbraUser $zimbraBinPath"/zmprov" modifyDomain "$ldapDomain" zimbraPasswordChangeListener singlepassword
        echo "Disabling auth fall back to local for $ldapDomain";
        sudo -u $zimbraUser $zimbraBinPath"/zmprov" modifyDomain "$ldapDomain" zimbraAuthFallbackToLocal FALSE
done

echo 'Restarting Zimbra';
sudo -u $zimbraUser $zimbraBinPath"/zmcontrol" restart
echo 'Done';

#to disable custom auth
#sudo -u zimbra /opt/zimbra/bin/zmprov modifyDomain "$domain" zimbraAuthMech zimbra