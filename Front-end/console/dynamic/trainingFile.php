<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 23/10/16
 * Time: 21:42
 */
    require '../../pages/config.php';

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }

    $response = \hutoma\console::existsAiTrainingFile($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);


    /* TODO need API call to do control response */

    echo ($response);
    unset($response);