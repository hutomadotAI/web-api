<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

$response = '';
$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

if (!isset($_FILES['imageFile'])) {
    echo 'Sorry upload failed. Please try again. If the problem persists, contact our support team.';
    exit;
}
if ($_FILES['imageFile']['error'] != UPLOAD_ERR_OK) {
    echo 'Sorry upload failed. Please try again. If the problem persists, contact our support team.';
    exit;
}
if (!is_uploaded_file($_FILES['imageFile']['tmp_name'])) {
    echo 'empty file';
    exit;
}

//$source_type = 0;
//$url = "";
$response = $aiApi->uploadFile($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $_FILES['inputfile'], 0, '');

unset($aiApi);


/**********  EVENTUALLY copy file to server-side
 * $filename = '/path/' . time() . $_SERVER['REMOTE_ADDR'] . 'txt';
 * if (!is_uploaded_file($_FILES['inputfile']['tmp_name']) || !copy($_FILES['inputfile']['tmp_name'], $filename)) {
 * $error = "Could not save file as $filename!";
 * echo  $_SERVER['DOCUMENT_ROOT'] ;
 * exit;
 * }
 */

echo json_encode($response, JSON_PRETTY_PRINT);
unset($response);
?>