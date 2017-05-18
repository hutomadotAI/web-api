<?php

namespace hutoma\api;

class botstoreApi extends apiBase
{
    private static $UIEndpointPath = "/ui";
    private static $botstorePath = "/botstore";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function getBotstoreList($botstoreQueryParam)
    {
        $query = http_build_query($botstoreQueryParam, null, '&');
        $query = str_replace('%2C', ',', str_replace('%25', '%', $query));

        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$UIEndpointPath . self::$botstorePath) . '?' . $query);
            $this->curl->setVerbGet();

            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getBotstoreBot($botId)
    {
        if (isset($botId)) {
            $this->curl->setUrl($this->buildRequestUrl(self::$UIEndpointPath . self::$botstorePath . '/' . $botId));
            $this->curl->setVerbGet();

            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}