#!/bin/bash
#install openjdk 1.7+
#install maven
mvn -f ../java/common/pom.xml clean install
mvn -f ../java/2fa/pom.xml clean package
cp -R ../java/2fa/target/2fa-1.0.jar ../dist
mvn -f ../java/zimbra-singlepassword-extension/pom.xml clean package
cp -R ../java/zimbra-singlepassword-extension/target/zimbra-singlepassword-extension.zip ../dist
ls -latrh ../dist