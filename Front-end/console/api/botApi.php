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
    private static $botstorePath = "/botstore";
    
    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }
    
    public function publishBot($bot)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$botstorePath));
            $this->curl->setVerbPost();

            $args =  array(
                'aiid' => $bot['aiid'],
                'alertMessage' => $bot['alertMessage'],
                //TODO remove hard code on badge after set NULL in API
                'badge' => 'fake badge',
                'category' => $bot['category'],
                'classification' =>  $bot['classification'],
                'description' => $bot['description'],
                //TODO ADD licenseType in API and in the new DB
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
            $this->handleApiCallError($curl_response, 385);
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