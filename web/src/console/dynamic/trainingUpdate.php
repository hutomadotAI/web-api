<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 21/10/16
 * Time: 18:36
 */
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}
$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$status = $aiApi->trainingUpdate($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($aiApi);
echo json_encode($status, JSON_PRETTY_PRINT);
unset($status);