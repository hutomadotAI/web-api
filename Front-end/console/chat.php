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

    $response = \hutoma\console::chatAI(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'],'1',$_GET['q'],'',false,0.5);

    if ($response['status']['code'] !== 200) {
        echo(json_encode($response,JSON_PRETTY_PRINT));
        unset($response);
        exit;
    }

    echo json_encode($response,JSON_PRETTY_PRINT);
    unset($response);
?>


