## Zimbra 2FA
 
**Leonardo Sampaio - Upwork - 2021-05-29
leonardors@gmail.com**

### Installation

If needed, define theses variables inside install.sh before execution:

	zimbraUser=zimbra
	jettyUser=$zimbraUser
	mysqlZimbraDb=zimbra
	mysqlRootUser=root
	zimbraPath=`echo "$localconfig" | grep -Po '(?<=^zimbra_home = ).*$'`
	jettyPath=$zimbraPath"/jetty"
	installFolder=$zimbraPath"/2fa"

To install do:

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