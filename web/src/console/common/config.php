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
}