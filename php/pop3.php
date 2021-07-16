<?php
$mbox = imap_open ("{mail.imanudin.net:995/pop3/ssl/novalidate-cert}INBOX", "admin", "lfJf8b4r");
var_dump($mbox);