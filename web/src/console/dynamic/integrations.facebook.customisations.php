<?php
require "../../pages/config.php";
require_once "../api/apiBase.php";
require_once "../../console/api/integrationApi.php";

if (!\hutoma\console::checkSessionIsActive()) {
    exit;
}

if (isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
    $aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
    $integrationApi = new \hutoma\api\integrationApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

    $data = json_decode(file_get_contents("php://input"));
    $page_greeting = $data->page_greeting;
    $get_started_payload = $data->get_started_payload;
    $integrationApi->setFacebookCustomisations($aiid, $page_greeting, $get_started_payload);
}

?>

