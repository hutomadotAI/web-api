<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/developerApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}

$developerApi = new \hutoma\api\developerApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

$response = $developerApi->updateDeveloperInfo(
    $_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'],
    $_POST['developer_name'],
    $_POST['developer_email'],
    $_POST['developer_address'],
    $_POST['developer_postcode'],
    $_POST['developer_city'],
    $_POST['developer_country'],
    $_POST['developer_company'],
    $_POST['developer_website']
);

unset($developerApi);

echo json_encode($response);
unset($response);

function isPostInputAvailable()
{
    return (
        isset($_POST['developer_name']) &&
        isset($_POST['developer_email']) &&
        isset($_POST['developer_address']) &&
        isset($_POST['developer_postcode']) &&
        isset($_POST['developer_city']) &&
        isset($_POST['developer_country']) &&
        isset($_POST['developer_company']) &&
        isset($_POST['developer_website'])
    );
}