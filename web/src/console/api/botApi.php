<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 05/01/17
 * Time: 21:12
 */

namespace hutoma\api;

class botApi extends apiBase
{
    private static $botstorePath = "/botstore";
    private static $path = "/ai";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function getPublishedBots()
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getPublishedBot($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/bot'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getPurchasedBots()
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath . '/purchased'));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getBotDetails($botId)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath . '/' . $botId, array('botId' => $botId)));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            //$this->handleApiCallError($curl_response, 100);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function getAiBotDetails($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/bot'));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function publishBot($bot)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath));
            $this->curl->setVerbPost();

            $args =  array(
                'aiid' => $bot['aiid'],
                'alertMessage' => $bot['alertMessage'],
                'badge' => $bot['badge'],
                'category' => $bot['category'],
                'classification' =>  $bot['classification'],
                'description' => $bot['description'],
                'licenseType' => $bot['licenseType'],
                'longDescription' => $bot['longDescription'],
                'name' => $bot['name'],
                'price' =>$bot['price'],
                'privacyPolicy' => $bot['privacyPolicy'],
                'sample' => $bot['sample'],
                'version' => $bot['version'],
                'videoLink' =>  $bot['videoLink'],
            );

            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getBotIcon($botId)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath . '/' . $botId . '/icon'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            return $curl_response;
        }
        return $this->getDefaultResponse();
    }


    public function uploadBotIcon($botId, $file)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath . '/' . $botId . '/icon'));

            $filename = $file['tmp_name'];
            $args['file'] = new \CurlFile($filename, 'image/png', 'icon.png');

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, $args);
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 386);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function purchaseBot($botId)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath . '/purchase/' . $botId));
            $this->curl->setVerbPost();
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