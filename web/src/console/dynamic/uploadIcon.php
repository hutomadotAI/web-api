<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/botApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_POST['botId'])) {
    echo json_encode(prepareResponse(500, 'Image upload failed - permission denied!'), true);
    exit;
}

if (!isset($_FILES['file'])) {
    echo json_encode(prepareResponse(500, 'Image upload failed - permission denied!'), true);
    exit;
}

if ($_FILES['file']['error'] != UPLOAD_ERR_OK) {
    echo json_encode(prepareResponse(500, 'Image upload failed - permission denied!.'), true);
    exit;
}


$botApi = new hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $botApi->uploadBotIcon($_POST['botId'], $_FILES['file']);
unset($botApi);

echo json_encode($response, true);
unset($response);

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}

?>