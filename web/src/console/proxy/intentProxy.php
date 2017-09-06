<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/ajaxApiProxy.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/intentsApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

class intentProxy extends ajaxApiProxy {

    public function onDelete($vars) {
        $aiid = sessionObject::getCurrentAI()['aiid'];
        $this->assertRequestVar($vars, 'intent', 400, 'Intent not provided');
        $intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $intentsApi->deleteIntent($aiid, $vars['intent']);
        unset($intentsApi);
        echo json_encode($result);
    }

    public function onUpdate($vars) {
        $this->assertRequestVar($vars, 'intent_name', 400, 'Intent name not provided');
        $this->assertRequestVar($vars, 'intent_expressions', 400, 'Expressions not provided');
        $this->assertRequestVar($vars, 'intent_responses', 400, 'Responses not provided');
        $this->assertRequestVar($vars, 'webhook', 400, 'Webhook not provided');
        $aiid = sessionObject::getCurrentAI()['aiid'];
        $vars['webhook']['aiid'] = $aiid;

        $intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $intentsApi->updateIntent(
            $aiid,
            $vars['intent_name'],
            $vars['intent_expressions'],
            $vars['intent_responses'],
            isset($vars['variables']) ? $vars['variables'] : null,
            $vars['webhook']
        );
        unset($intentsApi);
        echo json_encode($result);
    }

    public function onAdd($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }

    public function onGet($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }
}

$proxy = new intentProxy();
$proxy->handleRequest();
unset($proxy);

?>