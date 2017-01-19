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
$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$singleAI = $aiApi->getSingleAI($aiid);
unset($aiid);
unset($aiApi);
if ($singleAI['status']['code'] === 200) {
    $statusEx = array(
        "ai_status" => $singleAI['ai_status'],
        "phase_1_progress" => $singleAI['phase_1_progress'],
        "phase_2_progress" => $singleAI['phase_2_progress'],
        "training_file_uploaded" => $singleAI['training_file_uploaded'],
        "deep_learning_error" => $singleAI['deep_learning_error'],
        "api_status" => $singleAI['status']
    );
    echo json_encode($statusEx);
}
unset($singleAI);
