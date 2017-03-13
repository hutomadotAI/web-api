<?php
/**
 * Created by IntelliJ IDEA.
 * User: Andrea
 * Date: 30/09/16
 * Time: 13:21
 */
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'])){
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_POST['aiSkill'])) {
    echo json_encode(prepareResponse(500), true);
    exit;
}

$json = $_POST['aiSkill'];
$botSkill = json_decode($json, true);

foreach ($botSkill as $skill) {
    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    if ($skill['active'] == '0')
        $response = $aiApi->unlinkBotFromAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);
    else
        $response = $aiApi->linkBotToAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);
    unset($aiApi);

    if(!($response['status']['code']==200)){
        echo json_encode(prepareResponse(500), true);
        exit;
    }
}
echo json_encode(prepareResponse(200), true);


function prepareResponse($code)
{
    $arr = array('status' => array('code' => $code));
    return $arr;
}
?>