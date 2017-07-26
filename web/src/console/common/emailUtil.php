<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 26/07/17
 * Time: 11:32
 */

namespace hutoma;

require_once __DIR__ . '/curl.php';
require_once __DIR__ . '/config.php';

class emailUtil
{

    public static function sendEmail($to, $from, $subject, $htmlBody, $textBody)
    {

        $msg = array(
            "api_key" => config::getSmtp2GoApiToken(),
            "to" => array($to),
            "sender" => $from,
            "subject" => $subject,
            "html_body" => $htmlBody,
            "text_body" => $textBody
        );
        $jsonMsg = json_encode($msg);

        $curl = new curl("https://api.smtp2go.com/v3/email/send");
        $curl->setVerbPost();
        $curl->addHeader('Content-Type', 'application/json');
        $curl->setOpt(CURLOPT_POSTFIELDS, $jsonMsg);
        $curl_response = $curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response;
    }


}