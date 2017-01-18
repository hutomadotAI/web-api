<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/botApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_POST['botId'])) {
    echo json_encode(prepareResponse(500,'Sorry image upload failed. Permission denied!'), true);
    exit;
}

if (!isset($_POST['icon'])) {
    echo json_encode(prepareResponse(500,'Sorry image upload failed. Please try again. If the problem persists, contact our support team.'), true);
    exit;
}

$file = base64_decode(preg_replace('#^data:image/\w+;base64,#i', '', $_POST['icon']));

$botApi = new hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $botApi->uploadBotIcon($_POST['botId'], $file);
unset($botApi);
echo json_encode($response, true);
unset($response);

function prepareResponse($code,$info)
{
    $arr = array('status' => array('code' => $code,'info'=> $info));
    return $arr;
}
?>