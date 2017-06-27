<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 27/10/16
 * Time: 10:26
 */

namespace hutoma\api;


class integrationApi extends apiBase
{
    private static $aiPath = "/ai";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    private function buildIntegrationUrl($aiid)
    {
        return self::$aiPath . "/" . $aiid . "/facebook";
    }

    public function setConnectToken($aiid, $token, $redirectUri)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl($this->buildIntegrationUrl($aiid)."/connect", []));
            $this->curl->setVerbPost();
            $this->curl->addHeader('Content-Type', 'application/json');
            $this->curl->setOpt(CURLOPT_POSTFIELDS, json_encode(
                array(
                    'connect_token' => $token,
                    'redirect_uri' => $redirectUri
                )));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 310);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getFacebookConnectState($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl($this->buildIntegrationUrl($aiid), []));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 310);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function facebookAction($aiid, $params)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl($this->buildIntegrationUrl($aiid), $params));
            $this->curl->setVerbPut();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 310);
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