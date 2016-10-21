<?php
    require '../../pages/config.php';

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive()))
        \hutoma\console::redirect('../pages/login.php');

    $singleAI = \hutoma\console::getSingleAI( $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);

    if ($singleAI['status']['code'] === 200)
        echo $singleAI['deep_learning_error'];
    else
        echo('error');
    
    unset($singleAI);