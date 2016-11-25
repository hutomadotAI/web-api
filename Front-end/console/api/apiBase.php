<?php

namespace hutoma\api;

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
            \hutoma\console::redirect('./error.php?err=' . $errorCode);
        }

        $responseJson = json_decode($response);
        if (isset($responseJson) && $responseJson->status->code != 200 && $responseJson->status->code != 404 && $responseJson->status->code != 400) {
            telemetry::getInstance()->log(TelemetryEvent::ERROR, "api", $responseJson->status);
            $this->cleanup();
            \hutoma\console::redirect('./error.php?err=' . $errorCode);
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
        $finalPath = $this->getApiRequestBaseUrl() . $path;
        if (isset($params)) {
            $finalPath .= '?' . http_build_query($params);
        }
        return $finalPath;
    }

    protected function getApiRequestBaseUrl()
    {
        return \hutoma\console::getApiRequestUrl();
    }

    protected function getDefaultResponse()
    {
        return null;
    }
}