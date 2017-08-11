<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";


sessionObject::redirectToLoginIfUnauthenticated();

if (!isset(sessionObject::getCurrentAI()['aiid'])){
     exit;
}

if (!isset($_POST['aiSkill'])) {
    echo json_encode(prepareResponse(500), true);
    exit;
}

$json = $_POST['aiSkill'];
$botSkill = json_decode($json, true);

$result = "";
foreach ($botSkill as $skill) {
    $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    if ($skill['active'] == '0')
        $response = $aiApi->unlinkBotFromAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);
    else
        $response = $aiApi->linkBotToAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);
    unset($aiApi);

    $result = $response['status'];
    if($response['status']['code'] !== 200) {
        break;
    }
}
echo json_encode($result, true);

?>