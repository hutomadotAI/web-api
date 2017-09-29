<?php

namespace hutoma;

require_once __DIR__ . "/../common/errorRedirect.php";
require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/ajaxApiProxy.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/analyticsApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

class insightsProxy extends ajaxApiProxy {

    public function onAdd($vars) {
        $this->assertRequestVar($vars, 'aiid', 400, 'AIID not provided');
        $this->assertRequestVar($vars, 'fromDateIso', 400, 'From Date not provided');
        $this->assertRequestVar($vars, 'toDateIso', 400, 'To Date not provided');
        $this->assertRequestVar($vars, 'dataType', 400, 'Data Type not provided');

        $aiid = $vars['aiid'];
        $fromDateIso = $vars['fromDateIso'];
        $toDateIso = $vars['toDateIso'];
        $dataType = $vars['dataType'];
        $api = new api\analyticsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = null;
        switch($dataType) {
            case 'sessions': $result = $api->getChatSessions($aiid, $fromDateIso, $toDateIso); break;
            case 'interactions': $result = $api->getChatInteractions($aiid, $fromDateIso, $toDateIso); break;
            default: $result = errorRedirect::buildError(400, "Unknown requested data type"); break;
        }
        echo json_encode($result);
    }

    public function onDelete($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }

    public function onUpdate($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }

    public function onGet($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }
}

$proxy = new insightsProxy();
$proxy->handleRequest();
unset($proxy);

?>