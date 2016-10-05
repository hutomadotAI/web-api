<?php
require "../pages/config.php";
if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');


if(isset($_POST['action']) && !empty($_POST['action'])) {
        $action = $_POST['action'];
        switch($action) {
            case 'update' :
                $dev_token = \hutoma\console::getDevToken();
                $response = \hutoma\console::getSingleAI($dev_token,$_SESSION['aiid']);
                if ($response['status']['code']===200) {
                    $_SESSION['ai_status'] = $response['ai']['ai_status'];
                    $_SESSION['ai_deep_learning_error'] = $response['ai']['deep_learning_error'];

                    echo json_encode($response, JSON_PRETTY_PRINT);
                }
                else
                    echo 'error';
                break;
        }
    }
?>