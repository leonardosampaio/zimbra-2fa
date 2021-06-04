#!/bin/bash
#install openjdk 1.7+
#install maven
mvn -f ../java/common/pom.xml clean install
mvn -f ../java/2fa/pom.xml clean package
mvn -f ../java/zimbra-singlepassword-extension/pom.xml clean package