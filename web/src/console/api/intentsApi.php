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
            $this->handleApiCallError($curl_response, 317);
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
            $this->handleApiCallError($curl_response, 310);
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
            $this->handleApiCallError($curl_response, 311);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function updateIntent($aiid, $intentName, $expressions, $responses, $variables)
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
                    'variables' => $variables
                )
            ));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 311);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function createWebHook($aiid, $webHook)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid . self::$webHookPath,
                array('aiid' => $aiid, 'intent_name' => $webHook['intent_name'])));
            $this->curl->setVerbPost();
            $this->curl->addHeader('Content-Type', 'application/json');
            $this->curl->setOpt(CURLOPT_POSTFIELDS, json_encode(
                array(
                    'intent_name' => $webHook['intent_name'],
                    'endpoint' => $webHook['endpoint'],
                    'enabled' => $webHook['enabled'],
                )
            ));
            $curl_response = $this->curl->exec();
            //$this->handleApiCallError($curl_response, 313);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getWebHook($aiid, $intentName)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid . self::$webHookPath, array('aiid' => $aiid, 'intent_name' => $intentName)));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 313);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function updateWebHook($aiid, $webHook)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$intentPath . '/' . $aiid . self::$webHookPath));
            $this->curl->setVerbPost();

            $args = array(
                'intent_name' => $webHook['intent_name'],
                'endpoint' => $webHook['endpoint'],
                'enabled' => $webHook['enabled'],
            );

            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 314);
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