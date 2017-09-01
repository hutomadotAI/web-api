<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 18/05/17
 * Time: 10:52
 */

namespace hutoma\api;

include_once __DIR__ . "/apiBase.php";

class adminApi extends apiBase
{
    private static $path = "/admin";

    function __construct($sessionObject, $adminToken)
    {
        parent::__construct($sessionObject, $adminToken);
    }

    public function getDevToken($userId)
    {
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $userId . '/' . 'devToken'));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $this->handleApiCallError($curl_response);
        $json_response = json_decode($curl_response, true);
        $devToken = $json_response['dev_token'];
        return isset($devToken) ? $devToken : null;
    }

    public function userExists($username) {
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/user/exists',
            array('username' => $username, 'checkEmail' => true)));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $this->handleApiCallError($curl_response);
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] != 404;
    }

    public function register($id, $username, $saltedPass, $randomSalt, $fullname) {
        $params = array(
            'email' => $id,
            'username' => $username,
            'password' => $saltedPass,
            'password_salt' => $randomSalt,
            'first_name' => $fullname
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path, $params));
        $this->curl->setVerbPost();
        $curl_response = $this->curl->exec();
        $this->handleApiCallError($curl_response);
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] == 200;
    }

    public function updateUserLoginAttempts($user, $attempts) {
        $params = array(
            'loginAttempts' => $attempts,
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $user . '/loginAttempts'));
        $this->curl->setVerbPut();
        $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($params));
        $curl_response = $this->curl->exec();
        $this->handleApiCallError($curl_response);
        return $this->getDefaultResponse();
    }

    public function getUserInfo($username) {
        $params = array(
            'username' => $username,
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/user', $params));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $this->handleApiCallError($curl_response);
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] == 200 ? $json_response['user'] : null;
    }

    public function updateUserPassword($userId, $password, $passwordSalt) {
        $params = array(
            'user_id' => $userId,
            'password' => $password,
            'password_salt' => $passwordSalt
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/password'));
        $this->curl->setVerbPut();
        $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($params));
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] === 200;
    }

    public function isPasswordResetTokenValid($token) {
        $params = array(
            'token' => $token,
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/reset_token', $params));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] == 200;
    }

    public function deletePasswordResetToken($token) {
        $params = array(
            'token' => $token,
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/reset_token', $params));
        $this->curl->setVerbDelete();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] == 200;
    }

    public function insertPasswordResetToken($userId, $token) {
        $params = array(
            'user_id' => $userId,
            'token' => $token,
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/reset_token'));
        $this->curl->setVerbPost();
        $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($params));
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response['status']['code'] == 200;
    }

    public function getUserIdForToken($token) {
        $params = array(
            'token' => $token
        );
        $this->curl->setUrl($this->buildRequestUrl(self::$path . '/reset_token/user', $params));
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return isset($json_response['status']['code']) ? null : $json_response;
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}