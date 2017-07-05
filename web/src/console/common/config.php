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

    public static function getIntercomAppId() {
        $token = getenv("INTERCOM_APP_ID");
        if (isset($token) && $token != "") {
            return $token;
        }
        return null;
    }

    public static function getHotjarTrackingId() {
        $token = getenv("HOTJAR_TRACKING_ID");
        if (isset($token) && $token != "") {
            return $token;
        }
        return null;
    }

    public static function getRegistrationEmailDetails() {
        $details = array(
            "from" => "hello@hutoma.ai ",
            "reply-to" => "hello@hutoma.ai ",
        );
        return $details;
    }

    public static function getSalt() {
        return "^#$9%1f+2^p9)a@89)V$";
    }

    public static function getErrorPageUrl() {
        return "/console/error.php";
    }
}