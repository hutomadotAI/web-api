<?php

namespace hutoma;

require_once __DIR__ . "/../common/config.php";
require_once __DIR__ . "/../common/errorRedirect.php";
require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";
require_once __DIR__ . "/../common/utils.php";

\hutoma\sessionObject::redirectToLoginIfUnauthenticated();

$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$format = 'json';

$api = new \hutoma\api\aiApi(\hutoma\sessionObject::isLoggedIn(), \hutoma\sessionObject::getDevToken());
$exportedBot = $api->exportAI($aiid);
$botJson = json_encode($exportedBot['bot']);
if ($botJson == null || $exportedBot['status']['code'] != 200) {
    $err = '?errObj=' . urlencode($exportedBot);
    \hutoma\utils::redirect(\hutoma\config::getErrorPageUrl() . $err, null);
    exit;
}

if ($format == 'json') {
    header('Content-type: application/json');
    header('Accept-Ranges: bytes');
    header('Content-Disposition: attachment; filename="bot_' . $aiid . '.json"');
    echo $botJson;
}

