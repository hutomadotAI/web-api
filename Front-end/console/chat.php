<?php
   require "../pages/config.php";
    $question = $_GET['q'];


    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if ( !isset($_SESSION['aiid']) ){
        header('Location: ./error.php?err=2');
        exit;
    }



    $dev_token = \hutoma\console::getDevToken();
    $response = \hutoma\console::chatAI($dev_token,$_SESSION['aiid'],'1',$question,'',false,0.5);


    unset($dev_token);

    if ($response['status']['code'] !== 200) {
        echo(json_encode($response,JSON_PRETTY_PRINT));
        unset($response);
        exit;
    }

    echo json_encode($response,JSON_PRETTY_PRINT);
    unset($response);
?>


