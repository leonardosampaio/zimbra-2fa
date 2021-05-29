#!/bin/bash

domain=zimbra-docker.zimbra.io
zimbraUser=zimbra
zimbraPath=/opt/zimbra
jettyPath=$zimbraPath"/jetty-distribution-9.3.5.v20151012"
mysqlZimbraDb=zimbra
mysqlRootUser=root
mysqlRootPassword=`zmlocalconfig -s | grep -Po '(?<=^mysql_root_password = )\w*$'`

if [ "$(whoami)" != "$zimbraUser" ]; then
        echo "Invalid user, run as $zimbraUser"
        exit
fi

date=$(date '+%Y-%m-%d')
randomPassword=`date +%s | sha256sum | base64 | head -c 32 ; echo`

echo 'Creating database';
sed -ie "s/IDENTIFIED BY '.*';/IDENTIFIED BY '$randomPassword';/" sql/create.sql
mysql -u $mysqlRootUser -p$mysqlRootPassword $mysqlZimbraDb < sql/create.sql

echo 'Installing jar library';
cp -R dist/2fa-1.0.jar $jettyPath"/common/lib/"

echo 'Installing zimlets';
cp -R dist/br_com_sampaio_twofa_client.zip $zimbraPath"/zimlets"
cp -R dist/br_com_sampaio_twofa_admin.zip $zimbraPath"/zimlets"
zmzimletctl deploy $zimbraPath"/zimlets/br_com_sampaio_twofa_client.zip"
zmzimletctl deploy $zimbraPath"/zimlets/br_com_sampaio_twofa_admin.zip"

echo 'Modifing login page';
cp -R $jettyPath"/webapps/zimbra/public/login.jsp" $zimbraPath"/2fa/$date-login.jsp"
cp -R jsp/login.8.8.15.2fa.jsp $jettyPath"/webapps/zimbra/public/login.jsp"

echo 'Copying 2fa configuration file';
mkdir -p $zimbraPath"/2fa/"
cp -R config/config.properties $zimbraPath"/2fa/config.properties"
sed -ie "s/^mysqlPassword=.*/mysqlPassword=$randomPassword/" $zimbraPath"/2fa/config.properties"

echo 'Activating jsp files in zimlets';
zmprov ms "$domain" zimbraZimletJspEnabled TRUE

echo 'Restarting Zimbra';
zmcontrol restart