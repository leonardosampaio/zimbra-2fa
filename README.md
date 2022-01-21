## Zimbra 2FA

A two factor authentication implementation using client zimlet and a jar extension.

### Installation

If needed, define theses variables manually inside install.sh before execution:

	zimbraUser=zimbra
	jettyUser=`stat -c '%U' $jettyCommonLibDir`
	mysqlZimbraDb=zimbra
	mysqlRootUser=root
	zimbraPath=`echo "$localconfig" | grep -Po '(?<=^zimbra_home = ).*$'`
	jettyPath=$zimbraPath"/jetty"
	installFolder=$zimbraPath"/2fa"

To install, do:

	#put install.zip in a temp folder, e.g. /tmp/install
	unzip install.zip
	chmod +x install.sh
	sudo ./install.sh

Server will be restarted after installation.
### Testing

Use Docker image
	
	https://github.com/jorgedlcruz/zimbra-docker

Jetty logs in

	/opt/zimbra/log/zmmailboxd.out

Other logs in
	
	/opt/zimbra/log/
