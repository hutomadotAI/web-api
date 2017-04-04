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
require_once "../api/entityApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     \hutoma\console::redirect('/');
    exit;
}

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

$entityApi = new \hutoma\api\entityApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
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