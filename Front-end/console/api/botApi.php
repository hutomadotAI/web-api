<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 05/01/17
 * Time: 21:12
 */

namespace hutoma\api;
include "../common/bot.php";


class botApi extends apiBase
{
    private static $publishPath = "";
    private static $bot;
    
    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

 
    public function publishBot($devid, $bot)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$publishPath . '/' . $devid));
            $this->curl->setVerbPost();

/*
            $args =  array(
                'aiid' => $aiid,
                'name' => $name,
                'longDescription' => $longDescription,
                'alertMessage' => $alertMessage,
                'badge' => $badge,
                'price' => $price,
                'sample' => $sample,
                'category' => $category,
                'privacyPolicy' => $privacyPolicy,
                'price' => $price,
                'classification' => $classification,
                'version' => $version,
                'videoLink' => $videoLink
            );
*/
            
            $args = $bot->toArray();
            
            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 381);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getBotInfo(){
        $bot = new \hutoma\bot();
        $bot->getName();
        $bot->getDescription();
        $bot->getLongDescription();
        $bot->getUsecase();
        $bot->getAlarmMessage();
        $bot->getPrivacyLink();
        $bot->getUpdate();
        $bot->getLicenceType();
        $bot->getLicenceFee();
        $bot->getCategory();
        $bot->getClassification();
        $bot->getVersion();

        $arr = $bot->toArray();
        return $arr;
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}