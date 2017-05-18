<?php

namespace hutoma\api;

include_once __DIR__ . '/../common/apiConnector.php';
include_once __DIR__ . '/../common/config.php';
include_once __DIR__ . '/../common/utils.php';

use hutoma\telemetry;
use hutoma\TelemetryEvent;


/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 26/10/16
 * Time: 17:40
 */
class apiBase
{

    protected $curl;
    private $sessionObject;

    function __construct($sessionObject, $devToken)
    {
        $this->sessionObject = $sessionObject;
        $this->curl = new \hutoma\apiConnector(null, $devToken);
    }

    protected function isLoggedIn()
    {
        return $this->sessionObject;
    }

    protected function handleApiCallError($response, $errorCode)
    {
        if ($response === false) {
            telemetry::getInstance()->log(TelemetryEvent::ERROR, "api", "no response from api");
            $this->cleanup();
            \hutoma\utils::redirect('./error.php?err=' . $errorCode);
        }

        $responseJson = json_decode($response);
        if (isset($responseJson) && $responseJson->status->code != 200 && $responseJson->status->code != 404 && $responseJson->status->code != 400) {
            telemetry::getInstance()->log(TelemetryEvent::ERROR, "api", json_encode($responseJson->status));
            $this->cleanup();
            \hutoma\utils::redirect('./error.php?err=' . $errorCode);
        }
    }

    public function cleanup()
    {
        if (isset($this->curl)) {
            $this->curl->close();
        }
    }

    protected function __destruct()
    {
        $this->cleanup();
    }

    protected function buildRequestUrl($path, $params = null)
    {
        $finalPath = \hutoma\config::getApiRequestBaseUrl() . $path;
        if (isset($params)) {
            $finalPath .= '?' . http_build_query($params);
        }
        return $finalPath;
    }

    protected function getDefaultResponse()
    {
        return null;
    }
}