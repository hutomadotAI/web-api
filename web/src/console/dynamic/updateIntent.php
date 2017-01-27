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
require_once "../api/intentsApi.php";
require_once "../api/entityApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data'), true);
    exit;
}

$intentsApi = new \hutoma\api\intentsApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $intentsApi->updateIntent(
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'],
    $_POST['intent_name'],
    $_POST['intent_expressions'],
    $_POST['intent_responses'],
    $_POST['variables']
);

unset($intentsApi);

echo json_encode($response);
unset($response);


function isPostInputAvailable()
{
    return (
        isset($_POST['intent_name']) &&
        isset($_POST['intent_expressions']) &&
        isset($_POST['intent_responses'])
    );
}

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}
?>