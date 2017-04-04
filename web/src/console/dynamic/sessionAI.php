<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive()))
    exit;

if (!isset($_POST['ai'])) {
    \hutoma\console::redirect('./error.php?err=106');
    exit;
}


$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$singleAI = $aiApi->getSingleAI($_POST['ai']);
unset($aiApi);


switch ($singleAI['status']['code']) {
    case 200:
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
        \hutoma\console::redirect('../trainingAI.php');
        break;
    case 400:
        \hutoma\console::redirect('../error.php?err=200');
        break;
    default:
        \hutoma\console::redirect('../error.php?err=303');
}
unset($singleAI);
?>