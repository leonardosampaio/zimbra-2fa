## Zimbra 2FA
 
**Leonardo Sampaio - Upwork - 2021-05-29
leonardors@gmail.com**

### Installation

To install do:

	sudo mkdir -p /opt/zimbra/2fa
	#put install.zip inside /opt/zimbra/2fa
	sudo chown zimbra:zimbra /opt/zimbra/2fa
	su - zimbra
	cd /opt/zimbra/2fa
	unzip install.zip
	chmod +x install.sh
	./install.sh

Server will be restarted after installation.

If needed, define theses variables inside install.sh before execution:

	domain=zimbra-docker.zimbra.io
	zimbraUser=zimbra
	zimbraPath=/opt/zimbra
	jettyPath=$zimbraPath"/jetty-distribution-9.3.5.v20151012"
	mysqlZimbraDb=zimbra
	mysqlRootUser=root
	mysqlRootPassword=`zmlocalconfig -s | grep -Po '(?<=^mysql_root_password = )\w*$'`

### Testing

Use Docker image
	
	https://github.com/jorgedlcruz/zimbra-docker

### Troubleshooting

Jetty logs in

	/opt/zimbra/log/zmmailboxd.out

Other logs in
	
	/opt/zimbra/log/