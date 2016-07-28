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
    $response = hutoma\console::createAI( $dev_token, $_SESSION['ai_name'], $_SESSION['ai_description'], $_SESSION['ai_private'], $_SESSION['ai_confidence'], $_SESSION['ai_language'], $_SESSION['ai_timezone']);

    if ($response['status']['code'] === 200) {
        if (isset($_POST['userActivedDomains'])) {

            $userActivedList = json_decode($_POST['userActivedDomains'], true);

            foreach ($userActivedList as $key => $value)
                \hutoma\console::insertUserActiveDomain($dev_token, $response['aiid'], $key, $userActivedList[$key]);

            $_SESSION['aiid'] = $response['aiid'];
            $_SESSION['ai_created_on'] = '';
            $_SESSION['ai_training_status'] = 0;
            $_SESSION['ai_status'] = 0;
            $_SESSION['ai_deep_learning_error'] = 0.0;
            $_SESSION['ai_training_file'] = 0;

            $_SESSION['userActivedDomains'] = $_POST['userActivedDomains'];
            $_SESSION['current_ai_name'] = $_SESSION["ai_name"];
            unset($userActivedList);
        } else{
            unset($dev_token);
            unset($response);
            header('Location: ./error.php?err=4');
            exit();
        }
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
        isset($_SESSION['ai_private']) &&
        isset($_SESSION['ai_language']) &&
        isset($_SESSION['ai_timezone']) &&
        isset($_SESSION['ai_confidence']) &&
        isset($_SESSION['ai_description']) ;
}
?>
