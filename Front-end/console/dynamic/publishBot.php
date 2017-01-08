<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/botApi.php";

sleep(2);

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
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

echo($response);