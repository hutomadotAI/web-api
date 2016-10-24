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
    $status = \hutoma\console::trainingStop($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
    echo json_encode($status,JSON_PRETTY_PRINT);
    unset($status);