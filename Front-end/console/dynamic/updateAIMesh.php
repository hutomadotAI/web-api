<?php
/**
 * Created by IntelliJ IDEA.
 * User: Andrea
 * Date: 30/09/16
 * Time: 13:21
 */
require '../../pages/config.php';

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


//$arr = array('status' => array('code' => 200));
//echo( 'ok');

//$mesh = json_decode($_POST);
//storeAIMesh($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'],$mesh);
//unset($mesh);

function prepareResponse($code)
{
    $arr = array('status' => array('code' => $code),'AiSkill' => $_POST['AiSkill']);
    return $arr;
}

// I do not like remove Ai box when user deactives ones
function storeAIMesh($aiid,$mesh){

    // TODO need a check on api response on deleteAllMesh
    \hutoma\console::deleteAllMesh($aiid);

    if (is_array($mesh) || is_object($mesh)) {
        foreach ($mesh as $key => $value) {
            $boolean = json_decode($value);
            if ($boolean) {
                // TODO need a check on api response on addMesh
                $response = \hutoma\console::addMesh($aiid, $key);
            }
        }
        return true;
    }else
        return false;
}




?>