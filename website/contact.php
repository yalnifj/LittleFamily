<?php
    $subject = 'Little Family Tree - ' . (empty($_REQUEST['reason']) ? $_REQUEST['reason'] : 'no reason');
    $fromName = !empty($_REQUEST['name']) ? $_REQUEST['name'] : 'no name';
    $fromEmail = !empty($_REQUEST['email']) ? $_REQUEST['email'] : 'no email';
    $fhLevel = !empty($_REQUEST['fhLevel']) ? $_REQUEST['fhLevel'] : 'no fhLevel';
    $fsLevel = !empty($_REQUEST['fsLevel']) ? $_REQUEST['fsLevel'] : 'no fsLevel';
    $message = !empty($_REQUEST['message']) ? $_REQUEST['message'] : 'no message';

    $toEmail = 'yalnifj@gmail.com';
    $msg = "Little Family Tree Contact Form Submission\r\n".
        "Name: $fromName\r\n".
        "Email: $fromEmail\r\n".
        "Family History Level: $fhLevel\r\n".
        "FamilySearch Level: $fsLevel\r\n\r\n".
        $message;

    if (mail($toEmail, $subject, $msg) && mail($fromEmail, $subject, $msg)) {
        print 'success';
    } else {
        print 'fail';
    }
?>
