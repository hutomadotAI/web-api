<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/botApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_FILES['imageFile'])) {
    echo 'Sorry upload failed. Please try again. If the problem persists, contact our support team.';
    exit;
}
if ($_FILES['imageFile']['error'] != UPLOAD_ERR_OK) {
    echo 'Sorry upload failed. Please try again. If the problem persists, contact our support team.';
    exit;
}
if (!is_uploaded_file($_FILES['imageFile']['name'])) {
    echo 'empty file';
    exit;
}

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $botApi->uploadBotIcon(POST['aiid'], $_FILES['uploadImageFile']);

unset($botApi);
echo json_encode($response, JSON_PRETTY_PRINT);
unset($response);
?>