<?php
    require_once "../console/api/developerApi.php";

    $developerApi = new \hutoma\api\developerApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $developer = $developerApi->getDeveloperInfo($_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id']);
    unset($developerApi);

    if (isset($developer)) {
        switch ($developer['status']['code']) {
            case 200:
                include './dynamic/publishAI.content.publication.html.php';
                break;
            case 404:
                include './dynamic/publishAI.content.developer.html.php';
                break;
        }
    }
?>