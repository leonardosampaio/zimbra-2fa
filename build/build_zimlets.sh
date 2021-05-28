#!/bin/bash
cd ../zimlets/client
rm br_com_sampaio_twofa_client.zip
/usr/bin/zip br_com_sampaio_twofa_client.zip br_com_sampaio_twofa_client.js br_com_sampaio_twofa_client.xml br_com_sampaio_twofa_client.css 2fa.png  qrcode.jsp validate.jsp
cd ../admin
rm br_com_sampaio_twofa_admin.zip
/usr/bin/zip br_com_sampaio_twofa_admin.zip br_com_sampaio_twofa_admin.js br_com_sampaio_twofa_admin.properties br_com_sampaio_twofa_admin.xml invalidate.jsp