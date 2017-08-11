<?php
namespace hutoma;

    $developerApi = new api\developerApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    $developer = $developerApi->getDeveloperInfo(sessionObject::getCurrentUserInfoDetailsMap()['dev_id']);
    unset($developerApi);

    if (isset($developer)) {
        switch ($developer['status']['code']) {
            case 200:
                include __DIR__ . '/../dynamic/publishAI.content.publication.html.php';
                break;
            case 404:
                unset($developer);
                include __DIR__ . '/../dynamic/publishAI.content.developer.html.php';
                break;
        }
    }
?>
