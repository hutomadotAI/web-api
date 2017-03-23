<?php

namespace hutoma;

class webHook
{
    private $intentname;
    private $endpoint;
    private $enabled;

    public function __construct()
    {

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
        $webHook->setIntentName($botWebHook['intent_name']);
        $webHook->setEndpoint($botWebHook['endpoint']);
        $webHook->enableWebHook($botWebHook['enabled']);
        return $webHook;
    }

    public function toJSON()
    {
        $json = array(
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



