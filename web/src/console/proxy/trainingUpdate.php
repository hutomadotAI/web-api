<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();


$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$status = $aiApi->trainingUpdate(sessionObject::getCurrentAI()['aiid']);
unset($aiApi);
echo json_encode($status, JSON_PRETTY_PRINT);
unset($status);