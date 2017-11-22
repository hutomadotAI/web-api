<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/ajaxApiProxy.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/entityApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

class entityProxy extends ajaxApiProxy {

    public function onDelete($vars) {
        $this->assertRequestVar($vars, 'entity', 400, 'Entity not provided');
        $entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $entityApi->deleteEntity($vars['entity']);
        unset($entityApi);
        echo json_encode($result);
    }

    public function onUpdate($vars) {
        $this->assertRequestVar($vars, 'entity', 400, 'Entity not provided');
        $this->assertRequestVar($vars, 'values', 400, 'Values not provided');
        $entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $entityApi->updateEntity($vars['entity'], $vars['values']);
        unset($entityApi);
        echo json_encode($result);
    }

    public function onAdd($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }

    public function onGet($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }
}

$proxy = new entityProxy();
$proxy->handleRequest();
unset($proxy);

?>