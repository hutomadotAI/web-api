<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset(sessionObject::getCurrentAI()['aiid'])) {
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
        $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $responseFile = $aiApi->uploadFile(sessionObject::getCurrentAI()['aiid'], $_FILES['inputfile'], 0, '');
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
        $aiApiStructure = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $responseStructure = $aiApiStructure->uploadFile(sessionObject::getCurrentAI()['aiid'], $_FILES['inputstructure'], 1, '');
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


function prepareResponse($code, $info)
{
    $arr = array('status' => array('code' => $code, 'info' => $info));
    return $arr;
}

?>