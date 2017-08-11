<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset(sessionObject::getCurrentAI()['aiid'])) {
    utils::redirect('./error.php?err=2');
    exit;
}

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response = $aiApi->chatAI(
    sessionObject::getCurrentAI()['aiid'], // aiid
    $_GET['chatId'], // chatId
    $_GET['q']);
unset($aiApi);

if ($response['status']['code'] !== 200) {
    echo(json_encode($response, true));
    unset($response);
    exit;
}

echo json_encode($response, JSON_PRETTY_PRINT);
unset($response);
?>

