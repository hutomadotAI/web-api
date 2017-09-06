<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 11/08/17
 * Time: 16:12
 */

namespace hutoma;


abstract class ajaxApiProxy {

    public function handleRequest() {
        switch($_SERVER['REQUEST_METHOD']) {
            case 'GET':
                $this->onGet($_REQUEST);
                break;
            case 'DELETE':
                $this->onDelete($_REQUEST);
                break;
            case 'POST':
                $this->onAdd($_REQUEST);
                break;
            case 'PUT':
                // PHP doesn't handle the PUT entity body into $_REQUEST so we need to get it
                parse_str(file_get_contents("php://input"), $vars);
                $this->onUpdate(array_merge($_REQUEST, $vars));
                break;
        };
    }

    abstract public function onDelete($vars);
    abstract public function onUpdate($vars);
    abstract public function onAdd($vars);
    abstract public function onGet($vars);

    protected function assertRequestVar($vars, $varName, $errCode, $errInfo) {
        if (!isset($vars[$varName])) {
            echo $this->buildResponse($errCode, $errInfo);
            exit;
        }
    }

    protected function getApiReponseCode($response) {
        return $response['status']['code'];
    }

    protected function getApiResponseInfo($response) {
        return $response['status']['info'];
    }

    protected function buildResponse($code, $info) {
        return json_encode(array(
            'status' => array(
                'code' => $code,
                'info' => $info
            )
        ));
    }
}
