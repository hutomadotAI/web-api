<?php
/**
 * Created by IntelliJ IDEA.
 * User: bretc
 * Date: 31/08/2017
 * Time: 13:28
 */

namespace hutoma;

require_once __DIR__ . '/curl.php';
require_once __DIR__ . '/config.php';

class errorRedirect
{
    public static function handleErrorRedirect($result) {
        $code = $result['status']['code'];
        $info = $result['status']['info'];

        utils::redirect(config::getErrorPageUrl() . '?err=' . $code . '&msg=' . $info);
    }

    public static function defaultErrorRedirect() {
        utils::redirect(config::getErrorPageUrl() . '?err=500&msg=Internal%20Server%20Error');
    }
}
