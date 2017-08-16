<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();


$aiid = sessionObject::getCurrentAI()['aiid'];
$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$singleAI = $aiApi->getSingleAI($aiid);
unset($aiid);
unset($aiApi);
if ($singleAI['status']['code'] === 200) {
    $statusEx = array(
        "ai_status" => $singleAI['ai_status'],
        "phase_1_progress" => $singleAI['phase_1_progress'],
        "phase_2_progress" => $singleAI['phase_2_progress'],
        "training_file_uploaded" => $singleAI['training_file_uploaded'],
        "deep_learning_error" => $singleAI['deep_learning_error'],
        "api_status" => $singleAI['status']
    );
    echo json_encode($statusEx);
}
unset($singleAI);
