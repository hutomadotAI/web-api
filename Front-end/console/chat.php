<?php
   require "../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');


if ( !isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']) ){
        header('Location: ./error.php?err=2');
        exit;
    }

    $response = \hutoma\console::chatAI(
        \hutoma\console::getDevToken(), // devId
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'], // aiid
        $_GET['chatId'], // chatId
        $_GET['q'], // question
        '', // history
        false, // fs
        0.5); // min_p

    if ($response['status']['code'] !== 200) {
        echo(json_encode($response,JSON_PRETTY_PRINT));
        unset($response);
        exit;
    }

    echo json_encode($response,JSON_PRETTY_PRINT);
    unset($response);
?>


