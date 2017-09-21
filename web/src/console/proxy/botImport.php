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

$api = new \hutoma\api\aiApi(\hutoma\sessionObject::isLoggedIn(), \hutoma\sessionObject::getDevToken());
$file = $_FILES['file'];

$result = $api->importAI($file);
if ($result['status']['code'] !== 201) {
    \hutoma\errorRedirect::handleErrorRedirect($result);
}

\hutoma\utils::redirect(\hutoma\config::getHomePageUrl());

