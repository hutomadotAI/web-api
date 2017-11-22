<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 26/10/16
 * Time: 17:39
 */

namespace hutoma\api;


class intentsApi extends apiBase
{
    private static $intentsPath = "/intents";
    private static $intentPath = "/intent";
    private static $webHookPath = "/webhook";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function deleteIntent($aiid, $intentName)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid, array('intent_name' => $intentName)));
            $this->curl->setVerbDelete();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getIntents($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentsPath . '/' . $aiid, array('aiid' => $aiid)));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getIntent($aiid, $name)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid, array('aiid' => $aiid, 'intent_name' => $name)));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function updateIntent($aiid, $intentName, $expressions, $responses, $variables, $webhook)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid,
                array('aiid' => $aiid, 'intent_name' => $intentName)));
            $this->curl->setVerbPost();
            $this->curl->addHeader('Content-Type', 'application/json');
            $this->curl->setOpt(CURLOPT_POSTFIELDS, json_encode(
                array(
                    'intent_name' => $intentName,
                    'user_says' => $expressions,
                    'responses' => $responses,
                    'variables' => $variables,
                    'webhook' => $webhook
                )
            ));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
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