<?php

namespace hutoma\api;

include_once __DIR__ . '/../common/errorRedirect.php';
include_once __DIR__ . '/../common/apiConnector.php';
include_once __DIR__ . '/../common/config.php';
include_once __DIR__ . '/../common/utils.php';
include_once __DIR__ . '/../common/logging.php';

use hutoma as base;


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
        $this->curl = new base\apiConnector(null, $devToken);
    }

    protected function isLoggedIn()
    {
        return $this->sessionObject;
    }

    /**
     * Handles API "errors" (statuses that are not 200, 400, 404 or 409) or if the request did not reach the API.
     * @param $response - the API response
     */
    protected function handleApiCallError($response)
    {
        $responseJson = json_decode($response, true);

        if ($response === false || !isset($responseJson)) {
            base\logging::error("no response from api");
            $this->cleanup();
            base\errorRedirect::defaultErrorRedirect();
        }

        switch ($responseJson['status']['code']) {
            case 200:
            case 400:
            case 404:
            case 409:
                break;
            default:
                $this->cleanup();
                base\logging::error($response);
                base\errorRedirect::handleErrorRedirect($responseJson);
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
        $finalPath = base\config::getApiRequestBaseUrl() . $path;
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