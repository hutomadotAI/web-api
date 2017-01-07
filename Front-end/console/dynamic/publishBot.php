<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/botApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_POST['bot'])) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}
//$data = json_decode(stripslashes($_POST['data']));
sleep(2);

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
echo (200);

