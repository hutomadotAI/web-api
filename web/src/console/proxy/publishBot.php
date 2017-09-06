<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/botApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset($_POST['bot'])) {
    utils::redirect('./error.php?err=110');
    exit;
}

$json = $_POST['bot'];
$bot = json_decode($json, true);

$botApi = new api\botApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response = $botApi->publishBot($bot);

unset($botApi);
echo json_encode ($response,true);

