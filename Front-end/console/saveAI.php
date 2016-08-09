<?php
    require '../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit();
    }

    if ( !isValuesSessionInputFilled() ) {
        header("Location: ./error.php?err=2");
        exit();
    }

    $dev_token = \hutoma\console::getDevToken();
    $response = hutoma\console::createAI( $dev_token, $_SESSION['ai_name'], $_SESSION['ai_description'], $_SESSION['ai_language'], $_SESSION['ai_timezone'], $_SESSION['ai_confidence'], $_SESSION['ai_personality']);
    unset($dev_token);

    if ($response['status']['code'] === 200) {

            $userActivedList = json_decode($_SESSION['userActivedDomains'], true);
            $details = \hutoma\console::getUser();
        
            foreach ($userActivedList as $key => $value)
                \hutoma\console::insertUserActiveDomain($_SESSION['dev_id'] , $response['aiid'], $key, $userActivedList[$key]);

            $_SESSION['aiid'] = $response['aiid'];
            $_SESSION['ai_created_on'] = '';
            $_SESSION['ai_training_status'] = 0;
            $_SESSION['ai_deep_learning_error'] = 0.0;
            $_SESSION["ai_training_debug_info"] = '';
            $_SESSION['ai_training_status'] = 0;
            $_SESSION['ai_status'] = 0;
            //$_SESSION['ai_training_file'] = '';       // parameter missing
            $_SESSION['current_ai_name'] = $_SESSION["ai_name"];
            unset($userActivedList);
    }
    else{
        unset($dev_token);
        unset($response);
        header('Location: ./error.php?err=5');
        exit();
    }

    unset($dev_token);
    unset($response);

    header('Location: trainingAI.php');


function isValuesSessionInputFilled(){
    return
        isset($_SESSION['ai_name']) &&
        isset($_SESSION['ai_description']) &&
        isset($_SESSION['ai_language']) &&
        isset($_SESSION['ai_timezone']) &&
        isset($_SESSION['ai_confidence']) &&
        isset($_SESSION['ai_personality']) &&
        isset($_SESSION['dev_id']) &&
        isset($_SESSION['userActivedDomains']);
}

?>
