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

// if the list of meshed AI is empty, exit because there is nothing to save
if(isset($_POST['AiSkill'])) {
    $response = storeAIMesh($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'],$_POST['AiSkill']);
    echo json_encode(prepareResponse(200), true);
} else {
    echo json_encode(prepareResponse(400), true);
}

function prepareResponse($code)
{
    $arr = array('status' => array('code' => $code),'AiSkill' => $_POST['AiSkill']);
    return $arr;
}

// TODO is better if we use update functionality
function storeAIMesh($aiid,$mesh){

    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    // TODO need a check on api response on deleteAllMesh
    $aiApi->deleteAllMesh($aiid);
    if (is_array($mesh) || is_object($mesh)) {
        foreach ($mesh as $key => $value) {
            $boolean = json_decode($value);
            if ($boolean) {
                // TODO need a check on api response on addMesh
                $response = $aiApi->addMesh($aiid, $key);
            }
        }
        return true;
    }else
        return false;
}




?>