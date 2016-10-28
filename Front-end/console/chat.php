<?php
require "../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }


    if ( !isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']) ){
        header('Location: ./error.php?err=2');
        exit;
    }

    $hist="";
    if(isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['history'])) {
        $hist = $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['history'];
    }

    $confidence="0.0";
    if(isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence'])) {
        $confidence = $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence'];
    }

    $topic="";
    if(isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['topic'])) {
        $topic = $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['topic'];
    }

    $response = \hutoma\console::chatAI(
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'], // aiid
        $_GET['chatId'], // chatId
        $_GET['q'], // question
        $hist, // history
        false, // fs
        $confidence, // min_p
        $topic);

    if ($response['status']['code'] !== 200) {
        echo(json_encode($response,JSON_PRETTY_PRINT));
        unset($response);
        exit;
    }

    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['history'] = '';
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['topic'] = '';

    if (isset($response) && array_key_exists('result', $response)) {
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['history'] = $response['result']['history'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['topic'] = $response['result']['topic_out'];
    }

    echo json_encode($response,JSON_PRETTY_PRINT);
    unset($response);
?>

