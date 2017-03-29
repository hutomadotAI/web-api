<?php

namespace hutoma;

class webHook
{
    private $aiid;
    private $intentname;
    private $endpoint;
    private $enabled;

    public function __construct()
    {

    }

    public function getAiid()
    {
        return $this->aiid;
    }

    public function setAiid($aiid)
    {
        $this->aiid = $aiid;
    }

    public function getIntentName()
    {
        return $this->intentname;
    }

    public function setIntentName($intentname)
    {
        $this->intentname = $intentname;
    }

    public function getEndpoint()
    {
        return $this->endpoint;
    }

    public function setEndpoint($endpoint)
    {
        $this->endpoint = $endpoint;
    }

    public function isEnabled()
    {
        return $this->enabled;
    }

    public function enableWebHook($enabled)
    {
        $this->enabled = $enabled;
    }


    public static function fromObject($botWebHook)
    {
        $webHook = new webHook();
        $webHook->setAiid($botWebHook['aiid']);
        $webHook->setIntentName($botWebHook['intent_name']);
        $webHook->setEndpoint($botWebHook['endpoint']);
        $webHook->enableWebHook($botWebHook['enabled']);
        return $webHook;
    }

    public function toJSON()
    {
        $json = array(
            'aiid' => $this->getAiid(),
            'intent_name' => $this->getIntentName(),
            'endpoint' => $this->getEndpoint(),
            'enabled' => $this->isEnabled()
        );
        return json_encode($json);
    }
    
    public function __destruct()
    {

    }
}



