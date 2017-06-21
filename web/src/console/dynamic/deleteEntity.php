<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/entityApi.php";

if(!\hutoma\console::checkSessionIsActive()){
     exit;
}

if (!isset($_REQUEST['deleteentity'])) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

$entityApi = new \hutoma\api\entityApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
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
