<?php
    require '../../pages/config.php';

    $singleAI = \hutoma\console::getSingleAI(\hutoma\console::getDevToken(), $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);

    if ($singleAI['status']['code'] === 200)
        echo $singleAI['deep_learning_error'];
    else
        echo('error');
    
    unset($singleAI);