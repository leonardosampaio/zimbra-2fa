#!/bin/bash

if [ "$(whoami)" != "root" ]; then
        echo "Invalid user, run as root"
        exit
fi

#######################################################################################

zimbraBinPath="/opt/zimbra/bin"
localconfig=`$zimbraBinPath/zmlocalconfig -s`
domain=`echo "$localconfig" | grep -Po '(?<=^zimbra_server_hostname = ).*$'`
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

date=$(date '+%Y-%m-%d')
randomPassword=`date +%s | sha256sum | base64 | head -c 32 ; echo`

echo 'Creating install folder'
sudo mkdir -p $installFolder"/backup"
sudo chown -R $zimbraUser:$zimbraUser $installFolder

echo 'Creating database';
sed -ie "s/CHANGETHIS/$randomPassword/g" sql/create.sql
sudo -u $mysqlUser $zimbraBinPath"/mysql" --verbose -u $mysqlRootUser -p$mysqlRootPassword $mysqlZimbraDb < sql/create.sql

echo 'Copying jar library';
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

echo 'Copying 2fa configuration file';
sudo -u $zimbraUser cp -R config/config.properties $installFolder"/config.properties"
sudo -u $zimbraUser /bin/sed -i "s/^mysqlPassword=.*/mysqlPassword=$randomPassword/" $installFolder"/config.properties"

echo 'Activating jsp files in zimlets';
sudo -u $zimbraUser $zimbraBinPath"/zmprov" ms "$domain" zimbraZimletJspEnabled TRUE

echo 'Installing single app password extension';
sudo -u $jettyUser install -d $zimbraBinPath"/lib/ext/singlepassword/"
sudo -u $jettyUser unzip -o dist/zimbra-singlepassword-extension.zip
sudo -u $jettyUser rsync -rt -i --delete zimbra-singlepassword-extension/ $zimbraBinPath"/lib/ext/singlepassword/"

echo 'Restarting Zimbra';
sudo -u $zimbraUser $zimbraBinPath"/zmcontrol" restart