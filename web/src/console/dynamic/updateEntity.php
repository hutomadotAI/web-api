<?php
namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/entityApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response = $entityApi->updateEntity(
    $_POST['entity_name'],
    $_POST['entity_values']
);

unset($intentsApi);

echo json_encode($response);
unset($response);


function isPostInputAvailable()
{
    return (
        isset($_POST['entity_name']) &&
        isset($_POST['entity_values'])
    );
}

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}
?>