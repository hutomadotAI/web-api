<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 28/01/17
 * Time: 22:29
 */

namespace hutoma;


class sessionObject
{
    public static function getDevToken()
    {
        return (isset($_SESSION) &&  isset($_SESSION['devToken'])) ? $_SESSION['devToken'] : null;
    }

    public static function setDevToken($devToken)
    {
        $_SESSION['devToken'] = $devToken;
    }

    public static function getLastActivity()
    {
        return $_SESSION['LAST_ACTIVITY'];
    }

    public static function setLastActivity($lastActivity)
    {
        $_SESSION['LAST_ACTIVITY'] = $lastActivity;
    }

    public static function isLoggedIn() {
        return isset($_SESSION['LoggedIn']) && $_SESSION['LoggedIn'];
    }

    public static function setLoggedin($loggedIn) {
        return $_SESSION['LoggedIn'] = $loggedIn;
    }

    public static function checkSessionIsActive()
    {
        define('TIMEOUT', 24 * 60 * 60); // 1 day, in seconds
        if (!self::isLoggedIn()
            || PHP_SESSION_ACTIVE != session_status()
            || (sessionObject::getLastActivity() != null
                && (time() - sessionObject::getLastActivity() > TIMEOUT))
        ) {
            // last request was more than 30 minutes ago
            session_unset();     // unset $_SESSION variable
            //session_destroy();   // destroy session
            utils::redirect('/');
            return false;
        }
        sessionObject::setLastActivity(time());
        return true;
    }
    
    public static function clear() {
        self::setDevToken(null);
        self::setLastActivity(null);
    }

    public static function getCurrentUsername() {
        return $_SESSION['navigation_id'];
    }

    public static function getUserInfo($username) {
        if (array_key_exists($username, $_SESSION)) {
            return $_SESSION[$username];
        }
        return null;
    }

    public static function getCurrentUserInfo() {
        return self::getUserInfo(self::getCurrentUsername());
    }

    public static function getUserInfoDetailsMap($username) {
        $map = self::getUserInfo($username);
        return $map['user_details'];
    }

    public static function getCurrentUserInfoDetailsMap() {
        return self::getUserInfoDetailsMap(self::getCurrentUsername());
    }

    public static function setUserInfoDetailsMap($username, $map) {
        $user = self::getUserInfo($username);
        if (!isset($user)) {
            $_SESSION[$username]['user_details'] = $map;
        } else {
            $user['user_details'] = $map;
        }
    }


    public static function populateSessionWithUserDetails($username) {
        $details = self::getUserInfoDetailsMap($username);
        if(!isset($details)) {
            $api = new api\adminApi(sessionObject::isLoggedIn(), config::getAdminToken());
            $userInfo = $api->getUserInfo($username);

            self::setUserInfoDetailsMap($username, array(
                'name' => $userInfo['name'],
                'username' => $userInfo['username'],
                'dev_id' => $userInfo['dev_id'],
                'user_joined' => utils::joinedSince($userInfo['created']),
                'created' => $userInfo['created'],
                'email' => $userInfo['email'],
                'id' => $userInfo['id']
            ));
            unset($api);
            unset($userInfo);
        }
    }

    public static function getCurrentAI() {
        return isset( $_SESSION[self::getCurrentUsername()]['user_details']['ai'])
            ? $_SESSION[self::getCurrentUsername()]['user_details']['ai']
            : null;
    }

    public static function populateCurrentAI($aiInfo) {
        $ai = array (
            'aiid' => $aiInfo['aiid'],
            'name' => $aiInfo['name'],
            'status' => $aiInfo['ai_status'],
            'trainingfile' => $aiInfo['training_file_uploaded'],
            'phase_1_progress' => $aiInfo['phase_1_progress'],
            'phase_2_progress' => $aiInfo['phase_2_progress'],
            'deep_learning_error' => $aiInfo['deep_learning_error'],
            'language' => $aiInfo['language'],
            'voice' => $aiInfo['voice']
        );
        $_SESSION[self::getCurrentUsername()]['user_details']['ai'] = $ai;
    }

    public static function redirectToLoginIfUnauthenticated() {
        if(!self::isLoggedIn()){
            utils::redirect(config::getLoginPageUrl());
        }
    }
}