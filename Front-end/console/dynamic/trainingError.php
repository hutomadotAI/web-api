<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 21/10/16
 * Time: 18:36
 */
    require '../../pages/config.php';

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }

    $error = \hutoma\console::getAiDeepLearningError($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
    

    /* TODO need API call to do control response
    if ($singleAI['status']['code'] === 200)
        echo $singleAI['deep_learning_error'];
    else
        echo('error');
    */
    echo ($error);
    unset($error);