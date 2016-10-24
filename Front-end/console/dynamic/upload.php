<?php
    require "../../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
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
    switch ($_POST['tab']){
         case 'file':
            if (!isset($_FILES['inputfile'])) {
                echo 'Upload file failed';
                exit;
            }
            if ($_FILES['inputfile']['error'] != UPLOAD_ERR_OK) {
                echo 'Something is gone wrong';
                exit;
            }
            if (!is_uploaded_file($_FILES['inputfile']['tmp_name'])) {
                echo 'empty file';
                exit;
            }

             //$source_type = 0;
             //$url = "";
             $response = hutoma\console::uploadFile(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'],$_FILES['inputfile'],0,'');
             break;

        case 'structure':
            
            if (!isset($_FILES['inputstructure'])) {
                echo 'Upload complex file failed';
                exit;
            }
            if ($_FILES['inputstructure']['error'] != UPLOAD_ERR_OK) {
                echo 'Something is gone wrong';
                exit;
            }
            if (!is_uploaded_file($_FILES['inputstructure']['tmp_name'])) {
                echo 'empty file';
                exit;
            }

            //$source_type = 0;
            //$url = "";
            $response = hutoma\console::uploadFile(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'],$_FILES['inputstructure'],1,'');

            break;

        case 'url':
            if (!isset($_POST['url'])) {
                echo 'Send URL failed';
                exit;
            }
            $response = hutoma\console::uploadURL(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'],$_POST['url']);
            break;
    }






/**********  EVENTUALLY copy file to server-side
$filename = '/path/' . time() . $_SERVER['REMOTE_ADDR'] . 'txt';
if (!is_uploaded_file($_FILES['inputfile']['tmp_name']) || !copy($_FILES['inputfile']['tmp_name'], $filename)) {
    $error = "Could not save file as $filename!";
    echo  $_SERVER['DOCUMENT_ROOT'] ;
    exit;
}
*/

echo json_encode($response,JSON_PRETTY_PRINT);
unset($response);
unset($filename);
?>