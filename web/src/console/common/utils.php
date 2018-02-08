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
        $pageURL = "https://";
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

    public static function toIsoDate($dateStr) {
        $dateTime = new \DateTime($dateStr);
        return $dateTime->format(\DateTime::ATOM);
    }

    public static function isPasswordComplex($password) {
        return preg_match('/\d+/', $password) == 1               // at least one digit
            && strlen($password) >= 6                           // minimum length of 6 chars
            && preg_match('/[a-z]/', $password) == 1    // at least a lowercase letter
            && preg_match('/[A-Z]/', $password) == 1;   // at least an uppercase letter
    }

    /**
     * Returns a string which shows the time since the user has joined
     */
    public static function joinedSince($created)
    {

        $timeFirst = strtotime($created);
        $timeSecond = strtotime("now");
        $memsince = $timeSecond - strtotime($created);
        $regged = date("n/j/Y", strtotime($created));

        $memfor = "";

        if ($memsince < 60) {
            $memfor = $memsince . " Seconds";
        } else if ($memsince < 120) {
            $memfor = floor($memsince / 60) . " Minute";
        } else if ($memsince < 3600 && $memsince > 120) {
            $memfor = floor($memsince / 60) . " Minutes";
        } else if ($memsince < 7200 && $memsince > 3600) {
            $memfor = floor($memsince / 3600) . " Hour";
        } else if ($memsince < 86400 && $memsince > 3600) {
            $memfor = floor($memsince / 3600) . " Hours";
        } else if ($memsince < 172800) {
            $memfor = floor($memsince / 86400) . " Day";
        } else if ($memsince < 604800 && $memsince > 172800) {
            $memfor = floor($memsince / 86400) . " Days";
        } else if ($memsince < 1209600 && $memsince > 604800) {
            $memfor = floor($memsince / 604800) . " Week";
        } else if ($memsince < 2419200 && $memsince > 1209600) {
            $memfor = floor($memsince / 604800) . " Weeks";
        } else if ($memsince < 4838400) {
            $memfor = floor($memsince / 2419200) . " Month";
        } else if ($memsince < 31536000 && $memsince > 4838400) {
            $memfor = floor($memsince / 2419200) . " Months";
        } else if ($memsince < 63072000) {
            $memfor = floor($memsince / 31536000) . " Year";
        } else if ($memsince > 63072000) {
            $memfor = floor($memsince / 31536000) . " Years";
        }
        return (string) $memfor;
    }
}