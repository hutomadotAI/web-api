<?php
/**
 * Created by IntelliJ IDEA.
 * User: Andrea
 * Date: 30/09/16
 * Time: 13:21
 */
require '../../pages/config.php';

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=110');
    exit;
}

$response = hutoma\console::updateAI(
    $_POST['aiid'],
    $_POST['description'],
    $_POST['private'],
    $_POST['language'],
    $_POST['timezone'],
    $_POST['personality'],
    $_POST['voice'],
    $_POST['confidence']
);

updateSessionVariables();

echo json_encode($response);
unset($response);


function updateSessionVariables()
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $_POST['description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = $_POST['private'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = $_POST['language'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $_POST['timezone'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $_POST['personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $_POST['voice'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $_POST['confidence'];
}

function isPostInputAvailable()
{
    return (
        isset($_POST['aiid']) &&
        isset($_POST['description']) &&
        isset($_POST['private']) &&
        isset($_POST['language']) &&
        isset($_POST['timezone']) &&
        isset($_POST['personality']) &&
        isset($_POST['voice']) &&
        isset($_POST['confidence'])
    );
}
?>