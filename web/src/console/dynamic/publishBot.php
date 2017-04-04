<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/botApi.php";


if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     exit;
}

if (!isset($_POST['bot'])) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}

$json = $_POST['bot'];
$bot = json_decode($json, true);

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $botApi->publishBot($bot);

unset($botApi);
echo json_encode ($response,true);

