<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (isset(sessionObject::getCurrentAI()['aiid'])) {

    $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    $response = $aiApi->deleteAI(sessionObject::getCurrentAI()['aiid']);
    unset($aiApi);

    if ($response['status']['code'] === 200) {
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']);
        unset($_SESSION[$_SESSION['navigation_id']]['user_details']['bot']);
    } else {
        unset($response);
        utils::redirect('../error.php?err=203');
        exit;
    }

    unset($response);
    utils::redirect('../home.php');
}



?>

