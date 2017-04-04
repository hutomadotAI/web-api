<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/developerApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     \hutoma\console::redirect('/');
    exit;
}

if (!isset($_POST['developer'])) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}

$json = $_POST['developer'];
$developer = json_decode($json, true);
$developerApi = new \hutoma\api\developerApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

$response = $developerApi->updateDeveloperInfo($_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'],$developer);
unset($developerApi);
echo json_encode ($response,true);

