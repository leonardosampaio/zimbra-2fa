#!/bin/bash
cd ..
rm install.zip
/usr/bin/zip -r install.zip \
    config \
    dist \
    jsp/login.8.8.15.2fa.jsp \
    sql \
    install.sh
