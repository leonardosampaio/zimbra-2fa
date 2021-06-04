# Zimbra Crowd Authentication Extension

This Zimbra extensions implements a custom auth handler which authenticates
Zimbra accounts against an installation of Atlassian Crowd.



## Installation

The extension bundled in the file `target/zimbra-singlepassword-extension.zip` has to
be deployed on the Zimbra server.  In a multi-server environment it has to
be deployed on all instances.

To deploy the extension, copy the file to the server(s) and execture the
following commands:

```
install -d /opt/zimbra/lib/ext/singlepassword/
unzip zimbra-singlepassword-extension.zip
rsync -rt -i --delete zimbra-singlepassword-extension/ /opt/zimbra/lib/ext/singlepassword/
sudo -i -u zimbra zmmailboxdctl restart
```

If the extension was installed and loaded properly, the file
`/opt/zimbra/log/mailbox.log` should contain an entry along the lines of

```
extensions - Single password authentication enabled for domains: (none)
```


## Configuration

To authenticate against Crowd it has to be enabled once per domain by setting
the value of the attribute `zimbraAuthMech` to `custom:crowd`.

If users should be able to change their password via the Zimbra Web UI (which
is recommended) the value of the attribute `zimbraPasswordChangeListener` has
to be set to the value `crowd`.

```
zmprov modifyDomain example.com zimbraAuthMech custom:singlepassword
zmmailboxdctl restart
```

While this will enable the integration with Crowd, the extension requires some
additional information to contact the server.  The built-in client has to be
configured via the attributes described in the [Crowd Documentation](https://confluence.atlassian.com/crowd/the-crowd-properties-file-98665664.html).

The following minimal information is required:

* The Crowd Server URL aka `crowd.server.url` (eg. `https://crowd.example.net:8443/crowd/`)
* The Crowd Application Name aka `application.name` (eg. `zimbra`)
* The Crowd Application Password aka `application.password` (eg `changeme`)

These settings loaded from the following places:

1. An optional `crowd.properties` file. The default location of this file is
   `/opt/zimbra/conf/crowd.properties` but it can be overridden by setting
   the system property `crowd.properties` via the `localconfig.xml` key
   `mailboxd_java_options`.

2. These values provied by the properties file can be overridden via dedicated
   options in `/opt/zimbra/conf/localconfig.xml`.  These are modelled after
   the properties with all dots replaced by underscores and the prefix
   `crowd_` prepended unless the attribute already starts with this string.

3. Finally the options can be overridden per domain by providing optional
   arguments to the authentication mechanism set via the `zimbraAuthMech`
   domain attribute.  These arguments are seperated by whitespace and can be
   quoted if required.  Just use the same key names as in the `localconfig.xml`
   file.

The recommended way is to set the values via `localconfig.xml`:

```
zmlocalconfig -e crowd_server_url=https://crowd.example.net:8443/crowd/
zmlocalconfig -e crowd_application_name=zimbra
zmlocalconfig -e crowd_application_password=changeme
zmmailboxdctl restart
```

And override them per domain if required:

```
zmprov modifyDomain example.org zimbraAuthMech 'custom:crowd crowd_application_name=zimbra2 "crowd_application_password=change me"'
zmmailboxdctl restart
```

The authenticator will now try to authenticate against the given Crowd setup.
Per default accounts are mapped by email address, ie. for each login a search
against the Crowd directory is performed.  This has some performance impact
and can result in more than one result which will cause an authentication
error.  Due to this it is recommended to set the Crowd username to
authenticate against explicitly for each Zimbra account via the
`zimbraForeignPrincipal` account attribute and the `crowd:` prefix:

```
zmprov modifyAccount john.doe@example.com +zimbraForeignPrincipal crowd:jdoe
```


## Development

### Interfaces

This Zimbra extension implements the Zimbra APIs documented here:

* https://github.com/Zimbra/zm-mailbox/blob/8.8.15/store/docs/extensions.md
* https://github.com/Zimbra/zm-mailbox/blob/8.8.15/store/docs/customauth.txt
* https://github.com/Zimbra/zm-mailbox/blob/8.8.15/store/docs/changepasswordlistener.txt

It uses the Atlassian Crowd Java Integration Libraries which are documented
here:

* https://docs.atlassian.com/atlassian-crowd/3.4.6/index.html?com/atlassian/crowd/integration/rest/service/RestCrowdClient.html
* https://developer.atlassian.com/server/crowd/creating-a-crowd-client-using-crowd-integration-libraries/

### Prerequisites

This project depends on some Zimbra libraries which have to be placed in
the `lib` directory since they aren't available via Maven Central.

Just execute the script `lib.sh` to pull and extract them from the Zimbra
repositories.  Alternatively you can copy them from an existing Zimbra
server:

```
rsync -rt -i --delete zimbra.example.com:/opt/zimbra/lib/jars/ lib/  --include 'zimbra*.jar' --exclude '*.jar'
```

### Compilation

This project is built with Maven.  Just execute the following command:

```
mvn package
```

This will create a bundle `target/zimbra-crowd-extension.zip` which contains
the extension plus all the required libraries.

### Testing

The project provides a [Vagrant](https://www.vagrantup.com/) environment
with two virtual machines:  One Zimbra VM called `zcs` and another for
Atlassian Crowd called `aux`.

To start the environment, just do a

```
vagrant up
```

The first start will take a while since the installation files for both VMs
are downloaded and the environment is installed.  Once everything is up, the
following services are available locally:

* [Zimbra Web UI (HTTP)](http://127.0.0.1:7080/)
* [Zimbra Web UI (HTTPS)](https://127.0.0.1:7443/)
* [Zimbra Admin UI (HTTPS)](https://127.0.0.1:7071/)
* [Crowd Console (HTTP)](http://127.0.0.1:8095/crowd/console/)

The administrative user for the Zimbra installation is called
`admin@zimbra.invalid` and has the password `changeme`.

The Zimbra setup has an additional preconfigured domain `crowd.invalid` which
will authenticate against the Crowd installation.  It will use the application
name `zimbra` and the password `changeme`.  The domain contains a single user
named `john.doe` which is explicitly mapped to the Crowd user `jdoe`.  To test
the authentication try to log in as the user `john.doe@crowd.invalid` with the
password which is set for the Crowd user `jdoe`.

The Crowd setup currently has to be finished manually.  To do so, just go to
the [Crowd Console](http://127.0.0.1:8095/crowd/console/) and enter your
(evaluation) license key.  Afterwards choose the following steps:

1. New Installation
2. Embedded database
3. Verify the Base URL
4. Create an Internal Directory with all the default settings
5. Create a Default Administrator `admin` with the Email Address
   `admin@zimbra.invalid`
6. Do not enable any Integrated Applications
7. Log in as the previously created `admin` user
8. [Configure a mail server](http://127.0.0.1:8095/crowd/console/secure/admin/mailserver!execute.action)
   at `127.0.0.1` using the address `admin@zimbra.invalid`
9. [Create an application](http://127.0.0.1:8095/crowd/console/secure/application/addapplicationdetails.action)
   of type Generic Application named `zimbra` and using the password
   `changeme`
10. Set the application URL to `http://zcs.zimbra.test` and the Remote IP
    Address to `192.0.2.42`
11. Choose the previously created Internal Directory
12. Allow all users to authenticate
13. [Create a user](http://127.0.0.1:8095/crowd/console/secure/user/add.action)
    named `jdoe` with the Email Address `john.doe@crowd.invalid`

Since the provisioning of the Vagrant VMs takes such a long time and the
license is tied to the Crowd server id, it is recommended not to destroy the
machines but to do a clean shutdown via

```
vagrant halt
```

To (re-)deploy the extension you can call the Vagrant provisioning again via
a command line like

```
mvn package && vagrant provision zcs
```


### Release

All development should be done on feature branches.  They must be merged to
`develop` first.  Once the current state of `develop` is ready for release,
use the following command to merge to `master` and release:

```
mvn clean package
mvn gitflow:release
```

Afterwards draft a new GitHub release from the tag and attach the file
`target/zimbra-crowd-extension.zip`.


## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may find a copy of the License in the file LICENSE or at

https://opensource.org/licenses/Apache-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## Acknowledgement

This extension was created by the joint effort of [Silpion](https://www.silpion.de/),
a Zimbra Gold Partner, and [iVentureGroup](https://www.iventuregroup.com/).
We are both located in Hamburg with subsidaries all over Germany.  If you
like to code on varying projects in a friendly atmosphere or have a knack for
running software at large scale, check out the job offerings on our websites.
