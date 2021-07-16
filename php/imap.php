<?php

$mbox = imap_open ("{mail.imanudin.net:993/imap/ssl/novalidate-cert}INBOX", "admin", "lfJf8b4r");
var_dump($mbox);