<?php
    require "../pages/config.php";

    if ( !\hutoma\console::isSessionActive()) {
        echo('sessione scaduta');
        header("Location: ./login.php");
        exit();
    }


    $dev_token = \hutoma\console::getDevToken();
    $response = hutoma\console::createAI(   $dev_token,
                                            $_SESSION["ai_name"],
                                            $_SESSION["ai_description"],
                                            $_SESSION["ai_type"],
                                            $_SESSION["ai_confidence"],
                                            $_SESSION["ai_language"],
                                            $_SESSION["ai_timezone"]
                                        );

    if ($response['status']['code'] === 200) {
        if (isset($_POST['userActivedDomains'])) {
            $userActivedList = json_decode($_POST['userActivedDomains'], true);

            foreach ($userActivedList as $key => $value)
                \hutoma\console::insertUserActiveDomain($dev_token, $response['aiid'], $key, $userActivedList[$key]);

            $_SESSION["userActivedDomains"] = $_POST['userActivedDomains'];
            $_SESSION["training_status"] = 0;
            $_SESSION["ai_status"] = 0;
            $_SESSION["deep_learning_error"] = 0.0;
            
            unset($userActivedList);
        } else
            echo('domains seletion failed');
    }
    else{
        echo('JSON response AI creation ERROR');
        unset($dev_token);
        unset($response);
        header("Location: ./login.php");
        exit();
    }

    unset($dev_token);
    unset($response);

    // passare attuale ( nuova ) AI come  currentAI in use
    $_SESSION['current_ai_name'] = $_SESSION["ai_name"];

    $_SESSION['aiid'] = $array['ai_list'][0]['aiid'];
    $_SESSION['ai_name'] = $array['ai_list'][0]['name'];
    $_SESSION['ai_type'] = $array['ai_list'][0]['is_private'];
    $_SESSION['ai_status'] =  $array['ai_list'][0]['ai_status'];
    $_SESSION['ai_created_on'] = $array['ai_list'][0]['created_on'];
    $_SESSION['ai_description']= $array['ai_list'][0]['description'];
    $_SESSION['ai_training_status'] =  $array['ai_list'][0]['training_status'];
    $_SESSION['ai_deep_learning_error'] =  $array['ai_list'][0]['deep_learning_error'];
    $_SESSION["ai_language"]= "english";                    //$array['ai_list'][0]['language'];
    $_SESSION["ai_timezone"] = "GMT +00:00 UTC (UTC)";      //$array['ai_list'][0]['timezone'];
    $_SESSION["ai_confidence"] = "5";                       //$array['ai_list'][0]['confidence'];




    header("Location: trainingAI.php");

} else
        echo('errore');

?>

