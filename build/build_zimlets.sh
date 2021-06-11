#!/bin/bash
cd ..
rm dist/br_com_sampaio_twofa_client.zip
cd zimlets/client
/usr/bin/zip ../../dist/br_com_sampaio_twofa_client.zip br_com_sampaio_twofa_client.js br_com_sampaio_twofa_client.xml br_com_sampaio_twofa_client.css 2fa.png  qrcode.jsp validate.jsp
cd ../../
rm dist/br_com_sampaio_twofa_admin.zip
cd zimlets/admin
/usr/bin/zip ../../dist/br_com_sampaio_twofa_admin.zip br_com_sampaio_twofa_admin.js br_com_sampaio_twofa_admin.properties br_com_sampaio_twofa_admin.xml invalidate.jsp change.jsp
cd ../../
ls -latrh dist