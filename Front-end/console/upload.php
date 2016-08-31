<?php
require "../pages/config.php";

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if ( !isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']) ){
        header('Location: ./error.php?err=2');
        exit;
    }

    if (!isset($_POST['tab'])) {
        echo('no select: '.$_POST['tab']);
        exit;
    }

    if ( !isset($_FILES['inputfile'.$_POST['tab']])) {
        echo 'Upload file failed';
        exit;
    }

    if ($_FILES['inputfile'.$_POST['tab']]['error'] != UPLOAD_ERR_OK ){
        echo 'During Upload processing something is gone wrong';
        exit;
    }

    if (!is_uploaded_file($_FILES['inputfile'.$_POST['tab']]['tmp_name'])) {
        echo 'empty file';
        exit;
    }

/**********  EVENTUALLY copy file to server-side
$filename = '/path/' . time() . $_SERVER['REMOTE_ADDR'] . 'txt';
if (!is_uploaded_file($_FILES['inputfile']['tmp_name']) || !copy($_FILES['inputfile']['tmp_name'], $filename)) {
    $error = "Could not save file as $filename!";
    echo  $_SERVER['DOCUMENT_ROOT'] ;
    exit;
}
*/


//$source_type = 0;
//$url = "";
$response = hutoma\console::uploadFile(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'],$_FILES['inputfile'.$_POST['tab']],0,'');

if ($response['status']['code'] !== 200) {
    echo(json_encode($response,JSON_PRETTY_PRINT));
    unset($response);
    unset($filename);
    exit;
}

echo json_encode($response,JSON_PRETTY_PRINT);
unset($response);
unset($filename);
?>