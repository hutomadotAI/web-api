<?php
    require '../../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if ( isPostInputAvailable() ) {
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract'] = $_POST['ai_contract'];
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type'] = $_POST['ai_payment_type'];
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price'] = $_POST['ai_price'];

    }

    $response = hutoma\console::createAI(   \hutoma\console::getDevToken(),
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description'],
                                            true,
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['sex'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price']
                                        );

    if ($response['status']['code'] === 200)
        CallGetSingleAI($response['aiid']);
    else{
        unset($_response);
        header("Location: ../error.php?err=5");
        exit;
    }
    unset($_response);

        /*
        MANCA IL DEV ID
        ù
        $userActivedList = json_decode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['userActivedDomains'] , true);
        foreach ($userActivedList as $key => $value)
           \hutoma\console::insertUserActiveDomain($singleAI['dev_id'] , $response['aiid'], $key, $userActivedList[$key]);
        unset($userActivedList);

        */
    header('Location: ../trainingAI.php');


    function isPostInputAvailable(){
        return  (
            isset($_POST['ai_contract']) &&
            isset($_POST['ai_payment_type']) &&
            isset($_POST['ai_price'])
        );
    }

    function CallGetSingleAI($aiid){
        $singleAI = \hutoma\console::getSingleAI(\hutoma\console::getDevToken(),$aiid);
        if ($singleAI['status']['code'] === 200) {
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'] = $singleAI['ai']['aiid'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['created_on'] = $singleAI['ai']['created_on'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['is_private'] = $singleAI['ai']['is_private'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['deep_learning_error'] = $singleAI['ai']['deep_learning_error'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_debug_info'] = $singleAI['ai']['training_debug_info'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_status'] = $singleAI['ai']['training_status'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['status'] = $singleAI['ai']['ai_status'];
            //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_file']  = $singleAI['ai']['training_file\''];
            unset($singleAI);
        }else{
            unset($response);
            unset($singleAI);
            header("Location: ../error.php?err=15");
            exit;
        }
    }

?>

