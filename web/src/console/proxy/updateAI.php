<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    utils::redirect(config::getErrorPageUrl() . './error.php?err=110');
    exit;
}
$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response = $aiApi->updateAI(
    $_POST['aiid'],
    $_POST['description'],
    $_POST['language'],
    $_POST['timezone'],
    $_POST['personality'],
    $_POST['voice'],
    $_POST['confidence'],
    $_POST['default_chat_responses']
);
unset($aiApi);

updateSessionVariables();

echo json_encode($response);
unset($response);

function updateSessionVariables()
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $_POST['description'];
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
        isset($_POST['language']) &&
        isset($_POST['timezone']) &&
        isset($_POST['personality']) &&
        isset($_POST['voice']) &&
        isset($_POST['confidence'])
    );
}
?>