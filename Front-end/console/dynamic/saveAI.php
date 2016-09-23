<?php
    require '../../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        \hutoma\console::redirect('./error.php?err=1');
        exit;
    }

    if ( !isPostInputAvailable() ) {
        \hutoma\console::redirect('./error.php?err=2');
        exit;
    }

    $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['userActivedDomains'] = $_POST['userActivedDomains'];


    if ( isPostSkipInputAvailable() ) {
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract'] = $_POST['ai_contract'];
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type'] = $_POST['ai_payment_type'];
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price'] = $_POST['ai_price'];
    }else{
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract'] = 'skipped';
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type'] = 'skipped';
        $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price'] = 'skipped';
    }



    $response = hutoma\console::createAI(   \hutoma\console::getDevToken(),
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['private'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['voice'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['contract'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['payment_type'],
                                            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['price']
                                        );


    if ($response['status']['code'] === 200) {
        if (updateData($response['aiid'])){
            unset($_response);
            \hutoma\console::redirect('../trainingAI.php');
        }
        else {
            unset($_response);
            \hutoma\console::redirect('./error.php?err=15');
            exit;
        }
    }
    else{
        unset($_response);
        \hutoma\console::redirect('./error.php?err=5');
        exit;
    }

    function isPostInputAvailable(){
        return  (
        isset($_POST['userActivedDomains'])
        );
    }

    function isPostSkipInputAvailable(){
        return  (
            isset($_POST['ai_contract']) &&
            isset($_POST['ai_payment_type']) &&
            isset($_POST['ai_price'])
        );
    }

    function updateData($aiid){
        $singleAI = \hutoma\console::getSingleAI(\hutoma\console::getDevToken(),$aiid);
    
        if ($singleAI['status']['code'] === 200) {
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $singleAI['name'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['descritpion'] = $singleAI['description'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['created_on'] = $singleAI['created_on'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['is_private'] = $singleAI['is_private'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
            //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_debug_info'] = $singleAI['ai']['training_debug_info'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status'] = $singleAI['training_status'];
            $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];
            //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_file']  = $singleAI['ai']['training_file\''];

            if ( updateUserActivedDomains($singleAI['dev_id'], $singleAI['aiid']) ){
                unset($singleAI);
                return true;
            }else{
                unset($singleAI);
                return false;
            }
        }
        unset($singleAI);
        return false;
    }

    function updateUserActivedDomains($dev_id,$aiid){
        $userActivedList = json_decode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['userActivedDomains'] , true);
        try {
            foreach ($userActivedList as $key => $value)
              \hutoma\console::insertUserActiveDomain($dev_id, $aiid, $key, $userActivedList[$key]);
            
        }
        catch (Exception $e) {
            unset($userActivedList);
            return false;
        }
        unset($userActivedList);
        return true;
    }

?>

