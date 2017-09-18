<?php
require "../../pages/config.php";
require_once "../common/errorRedirect.php";
require_once "../common/sessionObject.php";
require_once "../api/aiApi.php";
require_once "../common/utils.php";

\hutoma\sessionObject::redirectToLoginIfUnauthenticated();

$api = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$file = $_FILES['file'];

$result = $api->importAI($file);
if ($result['status']['code'] !== 201) {
    \hutoma\errorRedirect::handleErrorRedirect($result);
}

\hutoma\utils::redirect(\hutoma\config::getHomePageUrl());

