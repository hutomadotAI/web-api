<?php

namespace hutoma;

require_once __DIR__ . "/../common/errorRedirect.php";
require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/menuObj.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    error_log("missing post data after stage 1 create AI");
    errorRedirect::defaultErrorRedirect();
    exit;
}

setSessionVariablesFromPost();

utils::redirect('../newAIBotstore.php');

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