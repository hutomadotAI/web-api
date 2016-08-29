<?php
    require '../../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit();
    }

    if ( !isPostInputAvailable() ) {
        header("Location: ./error.php?err=2");
        exit();
    }


    // creation new AI
    $dev_token = \hutoma\console::getDevToken();
    $response = CallCreationAI($dev_token);

    if ($response['status']['code'] === 200) {

        $singleAI = CallGetSingleAI($dev_token,$response['aiid']);
        /*

        $userActivedList = json_decode($_POST['userActivedDomains'], true);
        foreach ($userActivedList as $key => $value)
           \hutoma\console::insertUserActiveDomain($singleAI['dev_id'] , $response['aiid'], $key, $userActivedList[$key]);
        unset($userActivedList);

        */
        if ($singleAI['status']['code']===200) {
            echo('<!DOCTYPE html>');
            echo('<html>');
            echo('<body>');
            echo('<form>');
            echo('<input type="hidden" name="ai_name"                 value="' . $_POST['ai_name'] . '">');
            echo('<input type="hidden" name="ai_description"          value="' . $_POST['ai_description'] . '">');
            echo('<input type="hidden" name="ai_language"             value="' . $_POST['ai_language'] . '">');
            echo('<input type="hidden" name="ai_timezone"             value="' . $_POST['ai_timezone'] . '">');
            echo('<input type="hidden" name="ai_confidence"           value="' . $_POST['ai_confidence'] . '">');
            echo('<input type="hidden" name="ai_personality"          value="' . $_POST['ai_personality'] . '">');
            echo('<input type="hidden" name="ai_sex"                  value="' . $_POST['ai_sex'] . '">');
            echo('<input type="hidden" name="ai_confidence"           value="' . $_POST['ai_confidence'] . '">');

            echo('<input type="hidden" name="ai_userActivedDomains"   value="' . $_POST['userActivedDomains'] . '">');

            echo('<input type="hidden" name="ai_contract"             value="' . $_POST['ai_contract'] . '">');
            echo('<input type="hidden" name="ai_payment_type"         value="' . $_POST['ai_payment_type'] . '">');
            echo(' <input type="hidden" name="ai_price"                value="' . $_POST['ai_price'] . '">');

            echo('<input type="hidden" name="ai_id"                   value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_created_on"           value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_training_status"      value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_deep_learning_error"  value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_training_debug_info"  value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_status"               value="' . $response['aiid'] . '">');
            echo('<input type="hidden" name="ai_training_file">');        // parameter missing
            echo('</form>');
            echo('</body>');
            echo('</html>');
        }
        else{
            header('Location: ./error.php?err=0');
        }
        unset($singleAI);
    }
    else{
            unset($dev_token);
            unset($response);
            header('Location: ./error.php?err=5');
            exit();
    }

    unset($dev_token);
    unset($response);

    header('Location: ../trainingAI.php');



function CallCreationAI($dev_token){
    $response = hutoma\console::createAI(
        $dev_token,
        $_POST['ai_name'],
        $_POST['ai_description'],
        $_POST['ai_language'],
        $_POST['ai_timezone'],
        $_POST['ai_confidence'],
        $_POST['ai_personality'],
        $_POST['ai_contract'],
        $_POST['ai_payment_type'],
        $_POST['ai_price']
    );
    return $response;
}


function CallGetSingleAI($dev_token,$aiid){
    $array = \hutoma\console::getSingleAI($dev_token,$aiid);
    if ($array['status']['code']===200)
        return($array);

        unset($array);
        return;

}

function isPostInputAvailable(){
    return  (
        isset($_POST['ai_name']) &&
        isset($_POST['ai_description']) &&
        isset($_POST['ai_language']) &&
        isset($_POST['ai_timezone']) &&
        isset($_POST['ai_confidence']) &&
        isset($_POST['ai_personality']) &&
        isset($_POST['ai_sex']) &&
        isset($_POST['userActivedDomains']) &&
        isset($_POST['ai_contract']) &&
        isset($_POST['ai_payment_type']) &&
        isset($_POST['ai_price'])
    );
}

?>

