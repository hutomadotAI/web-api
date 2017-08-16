<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/botApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset($_POST['botId'])) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

if (!isset($_FILES['inputfile'])) {
    echo json_encode(prepareResponse(500, 'Missing file data.'), true);
    exit;
}

if ($_FILES['inputfile']['error'] != UPLOAD_ERR_OK) {
    echo json_encode(prepareResponse(500, 'Image upload failed - permission denied!.'), true);
    exit;
}

$botApi = new api\botApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response = $botApi->uploadBotIcon($_POST['botId'], $_FILES['inputfile']);
unset($botApi);

echo json_encode($response, true);
unset($response);

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}

?>