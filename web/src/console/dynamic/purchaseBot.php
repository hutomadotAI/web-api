<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/botApi.php";
require_once "../common/utils.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

if (!isset($_POST['botId'])) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}

$botId = $_POST['botId'];
$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $botApi->purchaseBot($botId);
unset($botApi);
echo json_encode ($response,true);

