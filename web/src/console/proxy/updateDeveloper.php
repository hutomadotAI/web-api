<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/menuObj.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/developerApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset($_POST['developer'])) {
    utils::redirect('./error.php?err=110');
    exit;
}

$json = $_POST['developer'];
$developer = json_decode($json, true);
$developerApi = new api\developerApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

$response = $developerApi->updateDeveloperInfo(sessionObject::getCurrentUserInfoDetailsMap()['dev_id'], $developer);
unset($developerApi);
echo json_encode ($response,true);

