<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive()))
     \hutoma\console::redirect('/');

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

setSessionVariablesFromPost();

\hutoma\console::redirect('../NewAIBotstore.php');

function isPostInputAvailable()
{
    return (
        isset($_POST['ai_name']) &&
        isset($_POST['ai_description']) &&
        isset($_POST['ai_language']) &&
        isset($_POST['ai_timezone']) &&
        isset($_POST['ai_confidence']) &&
        isset($_POST['ai_personality']) &&
        isset($_POST['ai_voice'])
    );
}

function setSessionVariablesFromPost()
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $_POST['ai_name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $_POST['ai_description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = $_POST['ai_language'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $_POST['ai_timezone'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $_POST['ai_confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $_POST['ai_personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $_POST['ai_voice'];
}
?>