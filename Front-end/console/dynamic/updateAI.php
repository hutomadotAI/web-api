<?php
/**
 * Created by IntelliJ IDEA.
 * User: Andrea
 * Date: 30/09/16
 * Time: 13:21
 */
    require '../../pages/config.php';

    if (!isset($_POST['aiid']) || !isset($_POST['description']) || !isset($_POST['private'])   ) {
        \hutoma\console::redirect('./error.php?err=110');
        exit;
    }

    $response = hutoma\console::updateAI($_POST['aiid'],$_POST['description'],$_POST['private']);

    /* NEED API FIRST CALL TO CHECK RESPONSE
    if ($response['status']['code'] === 200) {
            unset($_response);
            echo 'false';
    }else
    */
    if (!$response) {
        unset($_response);
        echo 'false';
    }

    updateSessionVariables();
    unset($response);
    echo true;

    function updateSessionVariables(){
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description'] = $_POST['description'];
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['private'] = $_POST['private'];
    }

?>

