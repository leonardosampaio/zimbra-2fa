<?php

require __DIR__.'/vendor/autoload.php';

$smtp = new PHPMailer\PHPMailer\SMTP;
$smtp->do_debug = PHPMailer\PHPMailer\SMTP::DEBUG_CONNECTION;

$options = array(
    'ssl' => array(
    'verify_peer' => false,
    'verify_peer_name' => false,
    'allow_self_signed' => true
)
); 

try {
    if (!$smtp->connect('ssl://mail.imanudin.net', 465, 5, $options)) {
        throw new Exception('Connect failed');
    }
    if (!$smtp->hello(gethostname())) {
        throw new Exception('EHLO failed: ' . $smtp->getError()['error']);
    }
    
    $e = $smtp->getServerExtList();

    if (is_array($e) && array_key_exists('STARTTLS', $e)) {
        $tlsok = $smtp->startTLS();
        if (!$tlsok) {
            throw new Exception('Failed to start encryption: ' . $smtp->getError()['error']);
        }
        if (!$smtp->hello(gethostname())) {
            throw new Exception('EHLO (2) failed: ' . $smtp->getError()['error']);
        }
        $e = $smtp->getServerExtList();
    }

    if (is_array($e) && array_key_exists('AUTH', $e)) {
        if ($smtp->authenticate('admin', 'lfJf8b4r')) {
            echo "Connected ok!";
        } else {
            throw new Exception('Authentication failed: ' . $smtp->getError()['error']);
        }
    }
} catch (Exception $e) {
    echo 'SMTP error: ' . $e->getMessage(), "\n";
}