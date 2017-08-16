<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/analyticsApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$aiid = sessionObject::getCurrentAI()['aiid'];
$to = utils::toIsoDate($_REQUEST['to']);
$from = utils::toIsoDate($_REQUEST['from']);
$format = 'csv';

$api = new api\analyticsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$chatLogs = $api->downloadChatLogs($aiid, $from, $to, $format);

$chatLogsJson = json_decode($chatLogs);
if ($chatLogsJson != null && array_key_exists('status', $chatLogsJson)) {
    if ($chatLogsJson->status->code != 200) {
        $err = '?errObj=' . urlencode($chatLogs);
        utils::redirect(config::getErrorPageUrl() . $err, null);
        exit;
    }
}

if ($format == 'csv') {
    header('Content-type: application/csv');
    header('Accept-Ranges: bytes');
    header('Content-Disposition: attachment; filename="chatlogs.csv"');
    echo $chatLogs;
}

