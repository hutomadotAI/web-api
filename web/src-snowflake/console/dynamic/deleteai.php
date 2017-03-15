<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {

    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $response = $aiApi->deleteAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
    unset($aiApi);

    if ($response['status']['code'] === 200) {
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['bot']);
    } else {
        unset($response);
        \hutoma\console::redirect('../error.php?err=203');
        exit;
    }

    unset($response);
    \hutoma\console::redirect('../home.php');
}



?>

