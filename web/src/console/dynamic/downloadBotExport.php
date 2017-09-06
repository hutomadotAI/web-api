<?php
require "../../pages/config.php";
require_once "../api/aiApi.php";
require_once "../common/utils.php";

$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$format = 'json';

$api = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
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

