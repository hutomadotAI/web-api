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
    $_POST['developer_company'],
    $_POST['developer_email'],
    $_POST['developer_address'],
    $_POST['developer_postCode'],
    $_POST['developer_city'],
    $_POST['developer_country'],

    $_POST['developer_website']
);
unset($developerApi);

switch($response['code']){
    case 200:
        // UPDATED developer
        unset($response);
        \hutoma\console::redirect('./publishAI.php');
        break;
    case 404:
        // MISSING or NULL FIELD in  developer
        unset($response);
        \hutoma\console::redirect('./publishAI.php');
        break;
    case 500:
        // DEVELOPER JUST EXISTS
        unset($response);
        \hutoma\console::redirect('./publishAI.php');
        break;
}

function isPostInputAvailable()
{
    return (
        isset($_POST['developer_name']) &&
        isset($_POST['developer_email']) &&
        isset($_POST['developer_address']) &&
        isset($_POST['developer_postCode']) &&
        isset($_POST['developer_city']) &&
        isset($_POST['developer_country']) &&
        isset($_POST['developer_company']) &&
        isset($_POST['developer_website'])
    );
}