<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 09/08/17
 * Time: 15:11
 */

namespace hutoma;


class logging
{
    public static function error($message) {
        error_log($message);
    }

    public static function info($message) {
        error_log($message);
    }
}