<?php
    $subject = 'Little Family Tree - ' . (!empty($_REQUEST['reasoning']) ? $_REQUEST['reasoning'] : 'no reason');
    $fromName = !empty($_REQUEST['name']) ? $_REQUEST['name'] : 'no name';
    $fromEmail = !empty($_REQUEST['email']) ? $_REQUEST['email'] : 'no email';
    $fhLevel = !empty($_REQUEST['fhLevel']) ? $_REQUEST['fhLevel'] : 'no fhLevel';
    $fsLevel = !empty($_REQUEST['fsLevel']) ? $_REQUEST['fsLevel'] : 'no fsLevel';
    $message = !empty($_REQUEST['message']) ? $_REQUEST['message'] : 'no message';
    $reason = !empty($_REQUEST['reasoning']) ? $_REQUEST['reasoning'] : 'no reason';
    $platform = !empty($_REQUEST['platform']) ? $_REQUEST['platform'] : 'no platform';
    $device = !empty($_REQUEST['device']) ? $_REQUEST['device'] : 'no device';

    $toEmail = 'service@yellowforktech.com';
    $msg = "Little Family Tree Contact Form Submission\r\n".
        "Name: $fromName\r\n".
        "Email: $fromEmail\r\n".
        "Family History Level: $fhLevel\r\n".
        "FamilySearch Level: $fsLevel\r\n".
        "Platform: $platform\r\n".
        "Device: $device\r\n".
        "Reason: $reason\r\n\r\n".
        $message;

    if (mail($toEmail, $subject, $msg) && mail($fromEmail, $subject, $msg)) {
        print 'success';
    } else {
        print 'fail';
    }
?>
