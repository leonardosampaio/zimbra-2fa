#!/bin/bash
mvn clean package
docker cp target/zimbra-singlepassword-extension.zip zimbra2:/
docker exec -it zimbra2 /bin/bash -c \
'install -d /opt/zimbra/lib/ext/singlepassword/; unzip -o zimbra-singlepassword-extension.zip; rsync -rt -i --delete zimbra-singlepassword-extension/ /opt/zimbra/lib/ext/singlepassword/; sudo -i -u zimbra zmmailboxdctl restart'

#activate
#docker exec -it zimbra2 /bin/bash -c \
#'zmprov modifyDomain example.com zimbraAuthMech custom:singlepassword; zmmailboxdctl restart'

#logs 
#tail -f /opt/zimbra/log/mailbox.log