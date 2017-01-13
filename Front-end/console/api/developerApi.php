<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 05/01/17
 * Time: 21:12
 */

namespace hutoma\api;

class developerApi extends apiBase
{
    private static $developerPath = "/developer";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function getDeveloperInfo($devid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$developerPath. '/' . $devid));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 380);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function updateDeveloperInfo($devid, $developer)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$developerPath . '/' . $devid));
            $this->curl->setVerbPost();

            $args =  array(
                'name' => $developer['name'],
                'email' => $developer['email'],
                'address' =>  $developer['address'],
                'postCode' => $developer['postCode'],
                'city' => $developer['city'],
                'country' => $developer['country'],
                'company' => $developer['company'],
                'website' =>$developer['website'],
            );

            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
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