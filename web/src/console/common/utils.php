<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 18/05/17
 * Time: 08:35
 */

namespace hutoma;


class utils
{
    public static function redirect($url, $status = 302)
    {
        header("Location: $url", true, $status);
        exit;
    }
}