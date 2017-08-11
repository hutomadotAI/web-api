<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/entityApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset($_REQUEST['deleteentity'])) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$entityName = $_REQUEST['deleteentity'];
$result = $entityApi->deleteEntity($entityName);

unset($entityApi);
unset($entityName);
echo json_encode($result, true);
unset($result);

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}
