<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 18/05/17
 * Time: 15:45
 */

namespace hutoma\api;

include_once __DIR__ . "/apiBase.php";


class signupCodeApi extends apiBase
{
    private static $path = "/invite";

    function __construct($sessionObject, $adminToken)
    {
        parent::__construct($sessionObject, $adminToken);
    }

    public function inviteCodeValid($code)
    {
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $code));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        if (isset($json_response) && $json_response !== false) {
            return $json_response['status']['code'];
        }

        return "unknown";
    }

    public function redeemInviteCode($code, $username)
    {
        $params = array(
            'username' => $username
        );

        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $code . '/redeem', $params));
        $this->curl->setVerbPost();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        if (isset($json_response) && $json_response !== false) {
            return $json_response['status']['code'];
        }

        return "unknown";
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}