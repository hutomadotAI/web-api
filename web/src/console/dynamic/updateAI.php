<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    utils::redirect('./error.php?err=110');
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
    $_POST['confidence']
);
unset($aiApi);

// Update the session variables
sessionObject::getCurrentAI()['language'] = $_POST['language'];
sessionObject::getCurrentAI()['voice'] = $_POST['voice'];


echo json_encode($response);
unset($response);


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