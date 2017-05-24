<?php

namespace hutoma\api;

include_once __DIR__ . '/../common/apiConnector.php';
include_once __DIR__ . '/../common/config.php';
include_once __DIR__ . '/../common/utils.php';

use hutoma\telemetry;
use hutoma\TelemetryEvent;


/**
 * Base class for API calls.
 */
class apiBase
{

    protected $curl;
    private $sessionObject;

    /**
     * apiBase constructor.
     * @param $sessionObject
     * @param $devToken
     */
    function __construct($sessionObject, $devToken)
    {
        $this->sessionObject = $sessionObject;
        $this->curl = new \hutoma\apiConnector(null, $devToken);
    }

    protected function isLoggedIn()
    {
        return $this->sessionObject;
    }

    /**
     * Redirects to the error page.
     * @param $errorCode - the error code to be used on the error page
     */
    protected function redirectToErrorPage($errorCode) {
        \hutoma\utils::redirect(\hutoma\config::getErrorPageUrl() . '?err=' . $errorCode);
    }

    /**
     * Handles API "errors" (statuses that are not 200, 400 and 404) or if the request did not reach the API.
     * @param $response - the API response
     * @param $errorCode - the error code to be used on the error page
     */
    protected function handleApiCallError($response, $errorCode)
    {
        if ($response === false) {
            telemetry::getInstance()->log(TelemetryEvent::ERROR, "api", "no response from api");
            $this->cleanup();
            $this->redirectToErrorPage($errorCode);
        }

        $responseJson = json_decode($response);
        if (isset($responseJson) && $responseJson->status->code != 200 && $responseJson->status->code != 404 && $responseJson->status->code != 400) {
            telemetry::getInstance()->log(TelemetryEvent::ERROR, "api", json_encode($responseJson->status));
            $this->cleanup();
            $this->redirectToErrorPage($errorCode);
        }
    }

    /**
     * Cleans up the connection.
     */
    public function cleanup()
    {
        if (isset($this->curl)) {
            $this->curl->close();
        }
    }

    /**
     * Destructor.
     */
    protected function __destruct()
    {
        $this->cleanup();
    }

    /**
     * Builds the API request URL.
     * @param $path - the request path
     * @param null $params - the optional map of parameters
     * @return string the request response
     */
    protected function buildRequestUrl($path, $params = null)
    {
        $finalPath = \hutoma\config::getApiRequestBaseUrl() . $path;
        if (isset($params)) {
            $finalPath .= '?' . http_build_query($params);
        }
        return $finalPath;
    }

    /**
     * Returns a default response (for overloading).
     * @return null
     */
    protected function getDefaultResponse()
    {
        return null;
    }
}