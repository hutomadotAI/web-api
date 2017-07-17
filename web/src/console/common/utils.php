<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 18/05/17
 * Time: 08:35
 */

namespace hutoma;

include_once __DIR__ . '/config.php';


class utils
{
    public static function redirect($url, $status = 302)
    {
        header("Location: $url", true, $status);
        exit;
    }

    /**
     * Get the current page URL
     */
    public static function curPageURL()
    {
        $pageURL = 'http';
        if (isset($_SERVER["HTTPS"]) && $_SERVER["HTTPS"] == "on") {
            $pageURL .= "s";
        }
        $pageURL .= "://";
        if ($_SERVER["SERVER_PORT"] != "80") {
            $pageURL .= $_SERVER["SERVER_NAME"] . ":" . $_SERVER["SERVER_PORT"] . $_SERVER["REQUEST_URI"];
        } else {
            $pageURL .= $_SERVER["SERVER_NAME"] . $_SERVER["REQUEST_URI"];
        }
        return $pageURL;
    }

    /**
     * Get the current page path.
     * Eg: /mypage, /folder/mypage.php
     */
    public static function curPage()
    {
        $parts = parse_url(utils::curPageURL());
        return $parts["path"];
    }

    /**
     * Any mails need to be sent by logSys goes to here
     */
    public static function sendMail($email, $subject, $body)
    {
        $emailDetails = config::getRegistrationEmailDetails();
        $headers = array();
        $headers[] = "MIME-Version: 1.0";
        $headers[] = "Content-type: text/html; charset=iso-8859-1";
        $headers[] = "From: " . $emailDetails['from'];
        $headers[] = "Reply-To: " . $emailDetails['reply-to'];
        return mail($email, $subject, $body, implode("\r\n", $headers));
    }

    /**
     * Generate a Random String
     * $int - Whether numeric string should be output
     */
    public static function generateRandomString($length, $int = false)
    {
        $random_str = "";
        $chars = $int ? "0516243741506927589" : "subinsblogabcdefghijklmanopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        $size = strlen($chars) - 1;
        for ($i = 0; $i < $length; $i++) {
            $random_str .= $chars[rand(0, $size)];
        }
        return $random_str;
    }

    public static function getRandomSalt() {
        return self::generateRandomString(20);
    }

    public static function generatePassword($password, $salt) {
        return hash('sha256', $password . config::getSalt() . $salt);
    }

    public static function getGoogleAnalyticsTrackerObject()
    {
        $trackerObject = getenv("GOOGLE_ANALYTICS_TRACKER");
        if (isset($trackerObject) && $trackerObject != "") {
            return $trackerObject;
        }
        return null;
    }

    public static function toIsoDate($dateStr) {
        $dateTime = new \DateTime($dateStr);
        return $dateTime->format(\DateTime::ATOM);
    }
}