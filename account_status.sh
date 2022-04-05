#!/bin/bash

if [ "$(whoami)" != "root" ]; then
        echo "Invalid user, run as root";
        exit;
fi

echo "Setting environment variables";

#######################################################################################

zimbraBinPath="/opt/zimbra/bin"
localconfig=`$zimbraBinPath/zmlocalconfig -s`
ldapDomains=`$zimbraBinPath/zmprov gad`
mysqlRootUser=root
mysqlRootPassword=`echo "$localconfig" | grep -Po '(?<=^mysql_root_password = ).*$'`

zimbraUser=zimbra
mysqlUser=`ps -ef | grep mysql.sock | grep -v grep | awk '{ print $1 }'`

#######################################################################################

echo 'Listing 2fa status of each account';

for ldapDomain in $ldapDomains; do
        printf "\nDomain: $ldapDomain\n";
        users=`sudo -u $zimbraUser $zimbraBinPath"/zmprov" --ldap gaa -v $ldapDomain | grep -E '^mail:(.)*$' | awk '{print $2}'`
        for user in $users; do

            sql="SELECT case count(1)
                        when 0 then 'inactive' 
                        else 'active' end
                        as 'status'
                FROM 2fa.clients
                WHERE 
                        email = '$user'
                        AND validated = true;"

            validated=`sudo -u $mysqlUser $zimbraBinPath"/mysql" -u $mysqlRootUser -p$mysqlRootPassword 2fa -e "$sql" | grep -E 'active|inactive'`;
            printf "%60s %10s \n" $user $validated
        done
done