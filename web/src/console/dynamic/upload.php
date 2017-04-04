<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     exit;
}

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
    echo json_encode(prepareResponse(500, 'Missing AI Id info'), true);
    exit;
}

if (!isset($_POST['tab'])) {
    echo json_encode(prepareResponse(500, 'Missing TAB info'), true);
    exit;
}

switch ($_POST['tab']) {
    case 'file':
        if (!isset($_FILES['inputfile'])) {
            echo json_encode(prepareResponse(500, 'Upload file failed.'), true);
            exit;
        }
        if ($_FILES['inputfile']['error'] != UPLOAD_ERR_OK) {
            echo json_encode(prepareResponse(500, 'Something is gone wrong.'), true);
            exit;
        }
        if (!is_uploaded_file($_FILES['inputfile']['tmp_name'])) {
            echo json_encode(prepareResponse(500, 'Empty file.'), true);
            exit;
        }

        //$source_type = 0;
        //$url = "";
        $aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
        $responseFile = $aiApi->uploadFile($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $_FILES['inputfile'], 0, '');
        unset($aiApi);
        echo json_encode($responseFile, true);
        unset($responseFile);
        break;

    case 'structure':

        if (!isset($_FILES['inputstructure'])) {
            echo json_encode(prepareResponse(500, 'Upload complex file failed'), true);
            exit;
        }
        if ($_FILES['inputstructure']['error'] != UPLOAD_ERR_OK) {
            echo json_encode(prepareResponse(500, 'Something is gone wrong'), true);
            exit;
        }
        if (!is_uploaded_file($_FILES['inputstructure']['tmp_name'])) {
            echo json_encode(prepareResponse(500, 'Empty file'), true);
            exit;
        }

        //$source_type = 0;
        //$url = "";
        $aiApiStructure = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
        $responseStructure = $aiApiStructure->uploadFile($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $_FILES['inputstructure'], 1, '');
        unset($aiApiStructure);
        echo json_encode($responseStructure, true);
        unset($responseStructure);
        break;

    case 'url':
        if (!isset($_POST['url'])) {
            echo json_encode(prepareResponse(500, 'No upload URl available'), true);
            exit;
        }
        break;
}


/**********  EVENTUALLY copy file to server-side
 * $filename = '/path/' . time() . $_SERVER['REMOTE_ADDR'] . 'txt';
 * if (!is_uploaded_file($_FILES['inputfile']['tmp_name']) || !copy($_FILES['inputfile']['tmp_name'], $filename)) {
 * $error = "Could not save file as $filename!";
 * echo  $_SERVER['DOCUMENT_ROOT'] ;
 * exit;
 * }
 */

function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}

?>