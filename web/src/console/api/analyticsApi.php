<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 10/07/17
 * Time: 13:49
 */

namespace hutoma\api;


class analyticsApi extends apiBase
{
    private static $insightsPath = "/insights";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function downloadChatLogs($aiid, $from, $to, $format)
    {
        $params = array(
            'format' => $format,
            'from' => $from,
            'to' => $to
        );

        $url = $this->buildRequestUrl(self::$insightsPath . '/' . $aiid . '/chatlogs', $params);

        if ($this->isLoggedIn()) {
            $this->curl->setUrl($url);
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 999);
            return $curl_response;
        }
        return $this->getDefaultResponse();
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}