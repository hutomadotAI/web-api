<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 28/01/17
 * Time: 22:29
 */

namespace hutoma;


class sessionObject
{

    public static function getDevToken()
    {
        return (isset($_SESSION) &&  isset($_SESSION['devToken'])) ? $_SESSION['devToken'] : null;
    }

    public static function setDevToken($devToken)
    {
        $_SESSION['devToken'] = $devToken;
    }

    public static function getLastActivity()
    {
        return $_SESSION['LAST_ACTIVITY'];
    }

    public static function setLastActivity($lastActivity)
    {
        $_SESSION['LAST_ACTIVITY'] = $lastActivity;
    }
    
    public static function clear() {
        self::setDevToken(null);
        self::setLastActivity(null);
    }
}