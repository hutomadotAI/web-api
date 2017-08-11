<?php
namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";
require_once __DIR__ . "/../api/intentsApi.php";
require_once __DIR__ . "/../api/entityApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data'), true);
    exit;
}

$intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$aiid = sessionObject::getCurrentAI()['aiid'];
$_POST['webhook']['aiid'] = $aiid;
$response = $intentsApi->updateIntent(
    $aiid,
    $_POST['intent_name'],
    $_POST['intent_expressions'],
    $_POST['intent_responses'],
    isset($_POST['variables']) ? $_POST['variables'] : null,
    $_POST['webhook']
);

unset($intentsApi);

echo json_encode($response);
unset($response);


function isPostInputAvailable()
{
    return (
        isset($_POST['intent_name']) &&
        isset($_POST['intent_expressions']) &&
        isset($_POST['intent_responses']) &&
        isset($_POST['webhook'])
    );
}

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}
?>