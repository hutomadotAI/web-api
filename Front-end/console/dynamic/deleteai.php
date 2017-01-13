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

        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['userActivedDomains']);

        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['created_on']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['is_private']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_debug_info']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status']);
        //unset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_file']);

        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['contract']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['payment_type']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['price']);

    } else {
        unset($response);
        \hutoma\console::redirect('../error.php?err=203');
        exit;
    }

    unset($response);
    \hutoma\console::redirect('../home.php');
}
?>

