<?php

namespace hutoma;


date_default_timezone_set('Europe/London');

/**
 *
 * Disable XML entity loading.
 * Note: this will disable loading any external xml entities including
 * url loading in simplexml_load_file() and likely other libxml based functions that deal with URLs
 * as well as <xsl:import />
 */
if (function_exists('libxml_disable_entity_loader')) {
    libxml_disable_entity_loader(true);
}

require_once("console/common/config.php");
require_once("console/common/utils.php");
require_once("console/common/apiConnector.php");
require_once("console/api/adminApi.php");
require_once("console/api/signupCodeApi.php");
require_once("console/common/telemetry.php");
require_once("console/common/sessionObject.php");

use hutoma\api\adminApi;


class console
{

    /**
     * ------------
     * BEGIN CONFIG
     * ------------
     * Edit the configuraion
     */

    public static $default_config = array(
        /**
         * Basic Config of logSys
         */
        "basic" => array(
            "company" => "hutoma",
            "email" => "hello@hutoma.com",
            "email_callback" => 0
        ),

        /** Intercom ID acquired */
        "intercom_app_id" => "ts64euf8",

        /**
         * Keys used for encryption
         * DONT MAKE THIS PUBLIC
         */
        "keys" => array(
            /**
             * Changing cookie key will expire all current active login sessions
             */
            "cookie" => "ckxc436jd*^30f840v*9!@#$",
            /**
             * `salt` should not be changed after users are created
             */
            "salt" => "^#$9%1f+2^p9)a@89)V$"
        ),

        /**
         * Enable/Disable certain features
         */
        "features" => array(
            /**
             * Should I Call session_start();
             */
            "start_session" => true,
            /**
             * Enable/Disable Login using Username & E-Mail
             */
            "email_login" => true,
            /**
             * Enable/Disable `Remember Me` feature
             */
            "remember_me" => true,
            /**
             * Should \Fr\LS::init() be called automatically
             */
            "auto_init" => false,

            /**
             * Prevent Brute Forcing
             * ---------------------
             * By enabling this, logSys will deny login for the time mentioned
             * in the "brute_force"->"time_limit" seconds after "brute_force"->"tries"
             * number of incorrect login tries.
             */
            "block_brute_force" => true,

            /**
             * Two Step Login
             * --------------
             * By enabling this, a checking is done when user visits
             * whether the device he/she uses is approved by the user.
             * Allows the original user to revoke logins in other devices/places
             * Useful if the user forgot to logout in some place.
             */
            "two_step_login" => false
        ),

        /**
         * `Blocking Brute Force Attacks` options
         */
        "brute_force" => array(
            /**
             * No of tries alloted to each user
             */
            "tries" => 5,
            /**
             * The time IN SECONDS for which block from login action should be done after
             * incorrect login attempts. Use http://www.easysurf.cc/utime.htm#m60s
             * for converting minutes to seconds. Default : 5 minutes
             */
            "time_limit" => 300
        ),

        /**
         * Information about pages
         */
        "pages" => array(
            /**
             * Pages that doesn't require logging in.
             * Exclude login page, but include REGISTER page.
             * Use Relative links or $_SERVER['REQUEST_URI']
             */
            "no_login" => array(),
            /**
             * The login page. ex : /login.php or /accounts/login.php
             */
            "login_page" => "login.php",
            /**
             * The home page. The main page for logged in users.
             * logSys redirects to here after user logs in
             */
            "home_page" => "/console/home.php",
        ),

        /**
         * Settings about cookie creation
         */
        "cookies" => array(
            /**
             * Default : cookies expire in 30 days. The value is
             * for setting in strtotime() function
             * http://php.net/manual/en/function.strtotime.php
             */
            "expire" => "+30 days",
            "path" => "/",
            "domain" => "",
        )
    );

    /* ------------
     * END Config.
     * ------------
     * No more editing after this line.
     */

    public static $config = array();
    public static $loggedIn = false;
    public static $user = false;
    private static $constructed = false;
    private static $init_called = false;
    private static $cookie, $session, $remember_cookie;

    /**
     * Merge user config and default config
     * $direct is for knowing whether the function is called by self::construct()
     */
    public static function config($config = null, $direct = true)
    {
        if ($config != null) {
            self::$config = $config;
        }
        self::$config = array_replace_recursive(self::$default_config, self::$config);
        if ($direct == true) {
            self::construct();
        }

    }

    public static function construct($called_from = "")
    {

        if (self::$constructed === false) {
            self::config(null, false);
            self::$constructed = true;
            if (self::$config['features']['start_session'] === true) {
                session_start();
            }

            /**
             * Add the login page to the array of pages that doesn't need logging in
             */
            array_push(self::$config['pages']['no_login'], self::$config['pages']['login_page']);

            self::$cookie = isset($_COOKIE['logSyslogin']) ? $_COOKIE['logSyslogin'] : false;
            self::$session = isset($_SESSION['logSyscuruser']) ? $_SESSION['logSyscuruser'] : false;
            self::$remember_cookie = isset($_COOKIE['logSysrememberMe']) ? $_COOKIE['logSysrememberMe'] : false;

            $encUserID = hash("sha256", self::$config['keys']['cookie'] . self::$session . self::$config['keys']['cookie']);
            if (self::$cookie == $encUserID) {
                self::$loggedIn = true;
            } else {
                self::$loggedIn = false;
            }

            /**
             * If there is a Remember Me Cookie and the user is not logged in,
             * then log in the user with the ID in the remember cookie, if it
             * matches with the decrypted value in `logSyslogin` cookie
             */
            if (self::$config['features']['remember_me'] === true && self::$remember_cookie !== false && self::$loggedIn === false) {
                $encUserID = hash("sha256", self::$config['keys']['cookie'] . self::$remember_cookie . self::$config['keys']['cookie']);
                if (self::$cookie == $encUserID) {
                    self::$loggedIn = true;
                } else {
                    self::$loggedIn = false;
                }
                if (self::$loggedIn === true) {
                    $_SESSION['logSyscuruser'] = self::$remember_cookie;
                    self::$session = self::$remember_cookie;
                }
            }

            self::$user = self::$session;

            if (self::$config['features']['auto_init'] === true && $called_from != "logout" && $called_from != "login") {
                self::init();
            }
            return true;
        }
    }

    /**
     * Logout the current logged in user by deleting the cookies and destroying session
     */
    public static function logout()
    {
        self::log_info("logout", "UserId: " . $_SESSION['logSyscuruser']);
        self::construct("logout");
        session_destroy();
        setcookie("logSyslogin", "", time() - 10, self::$config['cookies']['path'], self::$config['cookies']['domain']);
        setcookie("logSysrememberMe", "", time() - 10, self::$config['cookies']['path'], self::$config['cookies']['domain']);

        sessionObject::clear();
        /**
         * Wait for the cookies to be removed, then redirect
         */
        usleep(2000);
        utils::redirect(self::$config['pages']['login_page']);
        return true;
    }

    /**
     * Log something in the Francium.log file.
     * To enable logging, make a file called "Francium.log" in the directory
     * where "class.logsys.php" file is situated
     */
    public static function log_info($tag, $msg)
    {
        self::log(TelemetryEvent::INFO, $tag, $msg);
    }

    private static function log($type, $tag, $msg)
    {
        $log_file = __DIR__ . "/hutoma.log";
        if (file_exists($log_file)) {
            if ($msg != "") {
                $message = "[" . date("Y-m-d H:i:s") . "] $msg";
                $fh = fopen($log_file, 'a');
                fwrite($fh, $message . "\n");
                fclose($fh);
            }
        }
        telemetry::getInstance()->log($type, $tag, $msg);
    }

    /**
     * Do a redirect
     */
    public static function redirect($url, $status = 302)
    {
        utils::redirect($url, $status);
    }

    /**
     * A function that will automatically redirect user according to his/her login status
     */
    public static function init()
    {
        self::construct();
        if (self::$loggedIn === true && array_search(utils::curPage(), self::$config['pages']['no_login']) !== false) {
            utils::redirect(self::$config['pages']['home_page']);
        } elseif (self::$loggedIn === false && array_search(utils::curPage(), self::$config['pages']['no_login']) === false) {
            utils::redirect(self::$config['pages']['login_page']);
        }
        self::$init_called = true;
    }

    public static function log_error($tag, $msg)
    {
        self::log(TelemetryEvent::ERROR, $tag, $msg);
    }

    /**
     * A function to register a user with passing the username, password
     * and optionally any other additional fields.
     */
    public static function register($id, $password, $username, $fullname, $created)
    {
        self::construct();
        if (self::userExists($id)) {
            return "exists";
        } else {
            $randomSalt = self::rand_string(20);
            $saltedPass = hash('sha256', $password . self::$config['keys']['salt'] . $randomSalt);

            $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
            if ($api->register($id, $username, $saltedPass, $randomSalt, $fullname)) {
                $subject = "Welcome to Hu:toma!";
                $body = "Congrats, you are all set! Your Hu:toma account is confirmed. Check our intro video at https://www.youtube.com/watch?v=__pO6wVvBEY, which will guide you through using the Hu:toma platform. You will also find a chat icon on every page, which should be your go-to place for support.\r\nThanks\r\n-The Hu:toma team";
                if (!utils::sendMail($id, $subject, $body)) {
                    self::log_error("registration", "Could not send welcome email to " . $id);
                }
                return 200;
            }
            return "unknown";
        }
    }

    /**
     * Check if user exists with their username/email given
     * $identification - Either email/username
     */
    public static function userExists($identification)
    {
        $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
        return $api->userExists($identification);
    }

    /**
     * Generate a Random String
     * $int - Whether numeric string should be output
     */
    public static function rand_string($length, $int = false)
    {
        $random_str = "";
        $chars = $int ? "0516243741506927589" : "subinsblogabcdefghijklmanopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        $size = strlen($chars) - 1;
        for ($i = 0; $i < $length; $i++) {
            $random_str .= $chars[rand(0, $size)];
        }
        return $random_str;
    }

    /**
     * -------------------------
     * End Extra Tools/Functions
     * -------------------------
     */

    public static function getAdminToken()
    {
        $token = config::getAdminToken();
        if (isset($token) && $token != "") {
            return $token;
        } else {
            self::log_error("getAdminToken", "Admin token not found");
        }
        return null;
    }

    /**
     * -------------------------
     * End Extra Tools/Functions
     * -------------------------
     */

    public static function getApiRequestUrl()
    {
        $url = config::getApiRequestBaseUrl();
        return isset($url) ? $url : self::$config["api"]["request_url"];
    }

    /**
     * A function to handle the Forgot Password process
     */
    public static function forgotPassword()
    {
        self::construct();
        $curStatus = "initial";  // The Current Status of Forgot Password process
        $api = new adminApi(self::isLoggedIn(), config::getAdminToken());

        if (!isset($_POST['logSysForgotPass']) && !isset($_GET['resetPassToken']) && !isset($_POST['logSysForgotPassChange'])) {
            $html = '<form action="' . utils::curPageURL() . '" method="POST">';
            $html .= "<div class='form-group has-feedback'>";
            $html .= "<input  type='email' class='form-control flat' id='logSysIdentification' placeholder='enter your email'  name='identification' />";
            $html .= "<span class='glyphicon glyphicon-envelope form-control-feedback'></span>";
            $html .= "</div>";
            $html .= "<p><button name='logSysForgotPass' class='btn btn-primary btn-block btn-flat' type='submit'>Reset Password</button></p>";
            $html .= "</form>";
            echo $html;
            /**
             * The user had moved to the reset password form ie she/he is currently seeing the forgot password form
             */
            $curStatus = "resetPasswordForm";
        } elseif (isset($_GET['resetPassToken']) && !isset($_POST['logSysForgotPassChange'])) {
            /**
             * The user gave the password reset token. Check if the token is valid.
             */
            $reset_pass_token = urldecode($_GET['resetPassToken']);
            if (!$api->isPasswordResetTokenValid($reset_pass_token)) {
                echo "<h3>Error : Wrong/Invalid Token</h3>";
                $curStatus = "invalidToken"; // The token user gave was not valid
            } else {
                /**
                 * The token is valid, display the new password form
                 */
                $html = "<p>The Token key was Authorized. Now, you can change the password</p>";
                $html .= "<form action='{$_SERVER['PHP_SELF']}' method='POST'>";
                $html .= "<input type='hidden' name='token' value='{$reset_pass_token}' />";
                $html .= "<label>";
                $html .= "<p>New Password</p>";
                $html .= "<input type='password' name='logSysForgotPassNewPassword' />";
                $html .= "</label><br/>";
                $html .= "<label>";
                $html .= "<p>Retype Password</p>";
                $html .= "<input type='password' name='logSysForgotPassRetypedPassword'/>";
                $html .= "</label><br/>";
                $html .= "<p><button name='logSysForgotPassChange'>Reset Password</button></p>";
                $html .= "</form>";
                echo $html;
                /**
                 * The token was correct, displayed the change/new password form
                 */
                $curStatus = "changePasswordForm";
            }
        } elseif (isset($_POST['logSysForgotPassChange']) && isset($_POST['logSysForgotPassNewPassword']) && isset($_POST['logSysForgotPassRetypedPassword'])) {
            $reset_pass_token = urldecode($_POST['token']);
            if (!$api->isPasswordResetTokenValid($reset_pass_token)) {
                echo "<h3>Error : Wrong/Invalid Token</h3>";
                $curStatus = "invalidToken"; // The token user gave was not valid
            } else {
                if ($_POST['logSysForgotPassNewPassword'] == "" || $_POST['logSysForgotPassRetypedPassword'] == "") {
                    echo "<h3>Error : Passwords Fields Left Blank</h3>";
                    $curStatus = "fieldsLeftBlank";
                } elseif ($_POST['logSysForgotPassNewPassword'] != $_POST['logSysForgotPassRetypedPassword']) {
                    echo "<h3>Error : Passwords Don't Match</h3>";
                    $curStatus = "passwordDontMatch"; // The new password and retype password submitted didn't match
                } else {
                    /**
                     * We must create a fake assumption that the user is logged in to
                     * change the password as \Fr\LS::changePassword()
                     * requires the user to be logged in.
                     */
                    self::$user = $api->getUserIdForToken($reset_pass_token);
                    self::$loggedIn = true;

                    if (self::changePassword($_POST['logSysForgotPassNewPassword'])) {
                        self::$user = false;
                        self::$loggedIn = false;

                        /**
                         * The token shall not be used again, so remove it.
                         */
                        if (!$api->deletePasswordResetToken($reset_pass_token)) {
                            echo "<h3>Error</h3><p>There was a problem resetting your password.</p>";
                        } else {
                            echo "<h3>Success : Password Reset Successful</h3><p>You may now login with your new password.</p>";
                            $curStatus = "passwordChanged"; // The password was successfully changed
                        }
                    }
                }
            }
        } elseif (isset($_POST['identification'])) {
            /**
             * Check if username/email is provided and if it's valid and exists
             */
            $identification = $_POST['identification'];
            if ($identification == "") {
                header("Location: reset.php"); /* Redirect browser */
                self::log_error("reset_pwd", "User without identification");
                exit();

            } else {
                $userInfo = $api->getUserInfo($identification);
                if ($userInfo === null) {
                    $curStatus = "userNotFound"; // The user with the identity given was not found in the users database
                    self::log_info("reset_pwd", "User '" . $identification . "' attempted to reset password, but user not found");
                } else {
                    $email = $userInfo['email'];
                    $uid = $userInfo['id'];

                    $token = utils::generateRandomString(40);
                    $api->insertPasswordResetToken($uid, $token);
                    $encodedToken = urlencode($token);
                    $subject = "Hu:toma Password Reset";
                    $body = "Hello, we got a request to reset your password. If you ignore this message, your password won't be changed. If you do want to change your password please follow this link :
                      <blockquote>
                        <a href='" . utils::curPageURL() . "?resetPassToken={$encodedToken}'>Reset Password : {$token}</a>
                      </blockquote><br/>Thanks!<br/>-the Hu:toma Team";
                    if (!utils::sendMail($email, $subject, $body)) {
                        echo "<p>There was a problem sending the reset e-mail. Please go back and try again.</p>";
                        self::log_error("reset_pwd", "User '" . $identification . "' attempted to reset password, but email could not be sent");
                        return $curStatus;
                    }
                    $curStatus = "emailSent"; // E-Mail has been sent
                    self::log_info("reset_pwd", "User '" . $identification . "' successfully requested password reset");
                }
                echo "<p>If you entered a valid username, you should be receiving shortly a password reset email in your inbox. Don't forget to check your spam folder too!</p>";
            }
        }
        unset($api);
        return $curStatus;
    }

    /**
     * A function that handles the logged in user to change her/his password
     */
    public static function changePassword($newpass)
    {
        self::construct();
        if (self::$loggedIn) {
            $randomSalt = utils::getRandomSalt();
            $saltedPass = utils::generatePassword($newpass, $randomSalt);
            $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
            $api->updateUserPassword(
                self::$user,
                $saltedPass,
                $randomSalt
            );
            return true;
        } else {
            echo "<h3>Error : Not Logged In</h3>";
            return "notLoggedIn";
        }
    }

    /**
     * Returns a string which shows the time since the user has joined
     */
    public static function joinedSince($user = null)
    {
        self::construct();
        if ($user == null) {
            $user = self::$user;
        }
        $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
        $userInfo = $api->getUserInfo($user);
        $created = $userInfo['created'];
        unset($api);

        $timeFirst = strtotime($created);
        $timeSecond = strtotime("now");
        $memsince = $timeSecond - strtotime($created);
        $regged = date("n/j/Y", strtotime($created));

        if ($memsince < 60) {
            $memfor = $memsince . " Seconds";
        } else if ($memsince < 120) {
            $memfor = floor($memsince / 60) . " Minute";
        } else if ($memsince < 3600 && $memsince > 120) {
            $memfor = floor($memsince / 60) . " Minutes";
        } else if ($memsince < 7200 && $memsince > 3600) {
            $memfor = floor($memsince / 3600) . " Hour";
        } else if ($memsince < 86400 && $memsince > 3600) {
            $memfor = floor($memsince / 3600) . " Hours";
        } else if ($memsince < 172800) {
            $memfor = floor($memsince / 86400) . " Day";
        } else if ($memsince < 604800 && $memsince > 172800) {
            $memfor = floor($memsince / 86400) . " Days";
        } else if ($memsince < 1209600 && $memsince > 604800) {
            $memfor = floor($memsince / 604800) . " Week";
        } else if ($memsince < 2419200 && $memsince > 1209600) {
            $memfor = floor($memsince / 604800) . " Weeks";
        } else if ($memsince < 4838400) {
            $memfor = floor($memsince / 2419200) . " Month";
        } else if ($memsince < 31536000 && $memsince > 4838400) {
            $memfor = floor($memsince / 2419200) . " Months";
        } else if ($memsince < 63072000) {
            $memfor = floor($memsince / 31536000) . " Year";
        } else if ($memsince > 63072000) {
            $memfor = floor($memsince / 31536000) . " Years";
        }
        return (string)$memfor;
    }

    /**
     * ---------------------
     * Extra Tools/Functions
     * ---------------------
     */


    /**
     * A function to login the user with the username and password.
     * As of version 0.4, it is required to include the remember_me parameter
     * when calling this function to avail the "Remember Me" feature.
     */
    public static function login($username, $password, $remember_me = false, $cookies = true)
    {
        self::construct("login");

        $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
        $userInfo = $api->getUserInfo($username);

        if ($userInfo == null) {
            // No such user like that
            return false;
        } else {
            $us_id = $userInfo['id'];
            $userId = $userInfo['dev_id'];
            $us_pass = $userInfo['encrypted_password'];
            $us_salt = $userInfo['password_salt'];
            $status = $userInfo['attempts'];
            $saltedPass = hash('sha256', $password . self::$config['keys']['salt'] . $us_salt);
            if (substr($status, 0, 2) == "b-") {
                $blockedTime = substr($status, 2);
                if (time() < $blockedTime) {
                    $blocked = true;
                    return array(
                        "status" => "blocked",
                        "minutes" => round(abs($blockedTime - time()) / 60, 0),
                        "seconds" => round(abs($blockedTime - time()) / 60 * 60, 2)
                    );
                } else {
                    // remove the block, because the time limit is over
                    $api->updateUserLoginAttempts($userId, "");
                }
            }
            /**
             * Why login if password is empty ?
             * --------------------------------
             * If using OAuth, you have to login someone without knowing their password,
             * this usage is helpful. But, it makes a serious security problem too.
             * Hence, before calling \Fr\LS::login() in the login page, it is
             * required to check whether the password fieldis left blank
             */
            if (!isset($blocked) && ($saltedPass == $us_pass || $password == "")) {
                self::log_info("login", "UserId: " . $userId);
                if ($cookies === true) {

                    $_SESSION['logSyscuruser'] = $us_id;
                    $_COOKIE['logSyscuruser'] = $userInfo['username'];
                    setcookie("logSyslogin", hash("sha256", self::$config['keys']['cookie'] . $us_id . self::$config['keys']['cookie']), strtotime(self::$config['cookies']['expire']), self::$config['cookies']['path'], self::$config['cookies']['domain']);

                    $_SESSION['navigation_id'] = $_COOKIE['logSyscuruser'];

                    if ($remember_me === true && self::$config['features']['remember_me'] === true) {
                        setcookie("logSysrememberMe", $us_id, strtotime(self::$config['cookies']['expire']), self::$config['cookies']['path'], self::$config['cookies']['domain']);
                    }
                    self::$loggedIn = true;

                    if (self::$config['features']['block_brute_force'] === true) {
                        /**
                         * If Brute Force Protection is Enabled,
                         * Reset the attempt status
                         */
                        $api->updateUserLoginAttempts($userId, "0");
                    }

                    // Store the dev token
                    sessionObject::setDevToken($userInfo['dev_token']);

                    // Redirect
                    if (self::$init_called) {
                        utils::redirect(self::$config['pages']['home_page']);
                    }
                    return true;
                } else {

                    self::log_info("no_cookies", "UserId: " . $us_id);
                    /**
                     * If cookies shouldn't be set,
                     * it means login() was called
                     * to get the user's ID. So, return it
                     */
                    return $us_id;
                }
            } else {
                /**
                 * Incorrect password
                 * ------------------
                 * Check if brute force protection is enabled
                 */
                if (self::$config['features']['block_brute_force'] === true) {
                    $max_tries = self::$config['brute_force']['tries'];

                    if ($status == "") {
                        // User was not logged in before
                        $api->updateUserLoginAttempts($userId, "1");
                    } else if ($status == $max_tries) {
                        /**
                         * Account Blocked. User will be only able to
                         * re-login at the time in UNIX timestamp
                         */
                        $eligible_for_next_login_time = strtotime("+" . self::$config['brute_force']['time_limit'] . " seconds", time());
                        $api->updateUserLoginAttempts($userId, "b-"); // eligible_for_next_login_time,
                        return array(
                            "status" => "blocked",
                            "minutes" => round(abs($eligible_for_next_login_time - time()) / 60, 0),
                            "seconds" => round(abs($eligible_for_next_login_time - time()) / 60 * 60, 2)
                        );
                    } else if ($status < $max_tries) {
                        // If the attempts are less than Max and not Max
                        $api->updateUserLoginAttempts($userId, $status + 1); // Increase the no of tries by +1.
                    }
                }
                return false;
            }
        }
    }

    public static function getDevId()
    {
        return $_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'];
    }

    public static function getDevToken()
    {
        if (sessionObject::getDevToken() === NULL) {
            $api = new adminApi(self::isLoggedIn(), config::getAdminToken());
            $token = $api->getDevToken(self::getDevId());
            if (isset($token)) {
                sessionObject::setDevToken($token);
            }
        }
        return sessionObject::getDevToken();
    }

    public static function isLoggedIn()
    {
        return self::$loggedIn;
    }

    public static function checkSessionIsActive()
    {
        define('TIMEOUT', 24 * 60 * 60); // 1 day, in seconds
        if (!self::$loggedIn
            || PHP_SESSION_ACTIVE != session_status()
            || (sessionObject::getLastActivity() != null
                && (time() - sessionObject::getLastActivity() > TIMEOUT))
        ) {
            // last request was more than 30 minutes ago
            session_unset();     // unset $_SESSION variable
            session_destroy();   // destroy session
            utils::redirect('/');
            return false;
        }
        sessionObject::setLastActivity(time());
        return true;
    }

}

?>