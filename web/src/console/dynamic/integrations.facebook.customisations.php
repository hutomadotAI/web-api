<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/integrationApi.php";


set_error_handler(function ($errno, $errstr, $errfile, $errline, $errcontext) {
    http_response_code(400);
    exit;
});

if (!sessionObject::isLoggedIn()) {
    http_response_code(400);
    exit;
}

$integrationApi = new api\integrationApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

if (!isset(sessionObject::getCurrentAI()['aiid'])) {
    http_response_code(400);
    exit;
}
$aiid = sessionObject::getCurrentAI()['aiid'];

$data = json_decode(file_get_contents("php://input"));
$page_greeting = $data->page_greeting;
$get_started_payload = $data->get_started_payload;
$integrationApi->setFacebookCustomisations($aiid, $page_greeting, $get_started_payload);


?>