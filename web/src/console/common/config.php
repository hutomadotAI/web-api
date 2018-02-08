<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 17/05/17
 * Time: 11:56
 */

namespace hutoma;

class config
{
    public static function getApiRequestBaseUrl()
    {
        $url = getenv("HUTOMA_API_URL");
        if (isset($url) && $url != "") {
            return $url;
        }
        return null;
    }

    public static function getAdminToken() {
        $token = getenv("API_ADMIN_DEVTOKEN");
        if (isset($token) && $token != "") {
            return $token;
        }
        return null;
    }

    public static function curloptSSLVerifyPeer() {
        $verifypeer = getenv("CURLOPT_SSL_VERIFYPEER");
        return isset($verifypeer) ? $verifypeer : true;
    }

    public static function curloptSSLVerifyHost() {
        $verifyhost = getenv("CURLOPT_SSL_VERIFYHOST");
        return isset($verifyhost) ? $verifyhost : 2;
    }

    public static function getSmtp2GoApiToken() {
        return getenv("SMTP2GO_API_TOKEN");
    }

    public static function getRegistrationEmailDetails() {
        $details = array(
            "from" => "Hutoma <hello@hutoma.ai>",
            "reply-to" => "Hutoma <hello@hutoma.ai>",
        );
        return $details;
    }

    public static function getSalt() {
        return "^#$9%1f+2^p9)a@89)V$";
    }

    public static function getCookie() {
        return "ckxc436jd*^30f840v*9!@#$";
    }

    public static function getCookieParams() {
        return array(
            /**
             * Default : cookies expire in 30 days. The value is
             * for setting in strtotime() function
             * http://php.net/manual/en/function.strtotime.php
             */
            "expire" => "+30 days",
            "path" => "/",
            "domain" => "",
        );
    }

    public static function getRememberMeFlag() {
        return true;
    }

    public static function getBlockBruteForceFlag() {
        return true;
    }

    public static function getBruteForceMaxTries() {
        return 5;
    }

    public static function getBruteForceTimeLimit() {
        return 300;
    }

    public static function getErrorPageUrl() {
        return "/console/error.php";
    }

    public static function getLoginPageUrl() {
        return "/pages/login.php";
    }

    public static function getHomePageUrl() {
        return "/console/home.php";
    }
}