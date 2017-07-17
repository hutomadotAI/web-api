<?php
require "../../pages/config.php";
require_once "../api/analyticsApi.php";
require_once "../common/utils.php";

$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$to = \hutoma\utils::toIsoDate($_REQUEST['to']);
$from = \hutoma\utils::toIsoDate($_REQUEST['from']);
$format = 'csv';

$api = new \hutoma\api\analyticsApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$chatLogs = $api->downloadChatLogs($aiid, $from, $to, $format);

$chatLogsJson = json_decode($chatLogs);
if ($chatLogsJson != null && array_key_exists('status', $chatLogsJson)) {
    if ($chatLogsJson->status->code != 200) {
        $err = '?errObj=' . urlencode($chatLogs);
        \hutoma\utils::redirect(\hutoma\config::getErrorPageUrl() . $err, null);
        exit;
    }
}

if ($format == 'csv') {
    header('Content-type: application/csv');
    header('Accept-Ranges: bytes');
    header('Content-Disposition: attachment; filename="chatlogs.csv"');
    echo $chatLogs;
}

