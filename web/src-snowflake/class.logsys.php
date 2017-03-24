<?php
namespace hutoma;

/** ANDREA **/
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

require_once("console/common/apiConnector.php");
require_once("console/common/telemetry.php");
require_once("console/common/sessionObject.php");

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

        /**
         * Database Configuration
         */
        "db" => array(
            "host" => 'https://api.hutoma.com/v1',
            "port" => '433',
            "username" => 'hutoma_caller',
            "password" => '>YR"khuN*.gF)V4#',
            "name" => 'hutoma',
            "table" => 'users',
            "token_table" => "resetTokens"
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
        ),

        /**
         * 2 Step Login
         */
        'two_step_login' => array(
            /**
             * Message to show before displaying "Enter Token" form.
             */
            'instruction' => '',

            /**
             * Callback when token is generated.
             * Used to send message to user (Phone/E-Mail)
             */
            'send_callback' => '',

            /**
             * The table to stoe user's sessions
             */
            'devices_table' => 'user_devices',

            /**
             * The length of token generated.
             * A low value is better for tokens sent via Mobile SMS
             */
            'token_length' => 4,

            /**
             * Whether the token should be numeric only ?
             * Default Token : Alphabetic + Numeric mixed strings
             */
            'numeric' => false,

            /**
             * The expire time of cookie that authorizes the device
             * to login using the user's account with 2 Step Verification
             * The value is for setting in strtotime() function
             * http://php.net/manual/en/function.strtotime.php
             */
            'expire' => '+45 days',

            /**
             * Should logSys checks if device is valid, everytime
             * logSys is initiated ie everytime a page loads
             * If you want to check only the first time a user loads
             * a page, then set the value to TRUE, else FALSE
             */
            'first_check_only' => true
        )
    );

    /* ------------
     * END Config.
     * ------------
     * No more editing after this line.
     */

    public static $config = array();
    public static $loggedIn = false;
    public static $db = true;
    public static $user = false;
    private static $constructed = false;
    private static $init_called = false;
    private static $cookie, $session, $remember_cookie, $dbh;
    private static $sessionObj = null;

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
            self::$sessionObj = new sessionObject();
            /**
             * Try connecting to Database Server
             */
            try {
                /**
                 * Add the login page to the array of pages that doesn't need logging in
                 */
                array_push(self::$config['pages']['no_login'], self::$config['pages']['login_page']);

                $connectionString = getenv("HUTOMA_API_DB_CONNECTION_STRING");
                if (!isset($connectionString) || $connectionString == "") {
                    self::$dbh = new \PDO("mysql:dbname=" . self::$config['db']['name'] . ";host=" . self::$config['db']['host'] . ";port=" . self::$config['db']['port'] . ";charset=utf8", self::$config['db']['username'], self::$config['db']['password']);
                } else {
                    self::$dbh = new \PDO($connectionString, getenv("HUTOMA_API_DB_USERNAME"), getenv("HUTOMA_API_DB_PASSWORD"));
                }

                self::$db = true;
                self::$cookie = isset($_COOKIE['logSyslogin']) ? $_COOKIE['logSyslogin'] : false;
                self::$session = isset($_SESSION['logSyscuruser']) ? $_SESSION['logSyscuruser'] : false;
                self::$remember_cookie = isset($_COOKIE['logSysrememberMe']) ? $_COOKIE['logSysrememberMe'] : false;

                // DEBUG self::$dbh->setAttribute( \PDO::ATTR_ERRMODE , \PDO::ERRMODE_EXCEPTION );

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

                /**
                 * Check if devices is authorized to use the account
                 */
                if (self::$config['features']['two_step_login'] === true && self::$loggedIn) {
                    $login_page = self::curPage() === self::$config['pages']['login_page'];

                    if (!isset($_COOKIE['logSysdevice']) && $login_page === false) {
                        /**
                         * The device cookie is not even set. So, logout
                         */
                        self::logout();
                        $called_from = "login";
                    } else if (self::$config['two_step_login']['first_check_only'] === false || (self::$config['two_step_login']['first_check_only'] === true && !isset($_SESSION['device_check']))) {
                        //TODO: 2-step-login is off at the moment - to wil have to be moved to a stored procedure to work
                        $sql = self::$dbh->prepare("SELECT '1' FROM `" . self::$config['two_step_login']['devices_table'] . "` WHERE `uid` = ? AND `token` = ?");
                        $sql->execute(array(self::$user, $_COOKIE['logSysdevice']));

                        /**
                         * Device not authorized, so remove device cookie & logout
                         */
                        if ($sql->fetchColumn() !== '1' && $login_page === false) {
                            setcookie("logSysdevice", "", time() - 10);
                            self::logout();
                            $called_from = "login";
                        } else {
                            $_SESSION['device_check'] = 1;
                        }
                    }
                }

                if (self::$config['features']['auto_init'] === true && $called_from != "logout" && $called_from != "login") {
                    self::init();
                }
                return true;
            } catch (\PDOException $e) {
                /**
                 * Couldn't connect to Database
                 */
                self::log_error("init", "Exception: " . $e->getMessage());
                return false;
            }
        }
    }

    /**
     * Get the current page path.
     * Eg: /mypage, /folder/mypage.php
     */
    public static function curPage()
    {
        $parts = parse_url(self::curPageURL());
        return $parts["path"];
    }

    /**
     * Get the current page URL
     */
    public static function curPageURL()
    {
        $pageURL = 'http';
        if (isset($_SERVER["HTTPS"]) && $_SERVER["HTTPS"] == "on") {
            $pageURL .= "s";
        }
        $pageURL .= "://";
        if ($_SERVER["SERVER_PORT"] != "80") {
            $pageURL .= $_SERVER["SERVER_NAME"] . ":" . $_SERVER["SERVER_PORT"] . $_SERVER["REQUEST_URI"];
        } else {
            $pageURL .= $_SERVER["SERVER_NAME"] . $_SERVER["REQUEST_URI"];
        }
        return $pageURL;
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

        self::$sessionObj->clear();
        /**
         * Wait for the cookies to be removed, then redirect
         */
        usleep(2000);
        self::redirect(self::$config['pages']['login_page']);
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
        header("Location: $url", true, $status);
        exit;
    }

    /**
     * A function that will automatically redirect user according to his/her login status
     */
    public static function init()
    {
        self::construct();
        if (self::$loggedIn === true && array_search(self::curPage(), self::$config['pages']['no_login']) !== false) {
            self::redirect(self::$config['pages']['home_page']);
        } elseif (self::$loggedIn === false && array_search(self::curPage(), self::$config['pages']['no_login']) === false) {
            self::redirect(self::$config['pages']['login_page']);
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
        if (self::userExists($id) || (isset($other['email']) && self::userExists($other['email']))) {
            return "exists";
        } else {
            $randomSalt = self::rand_string(20);
            $saltedPass = hash('sha256', $password . self::$config['keys']['salt'] . $randomSalt);
            $dev_token = self::getAdminToken();
            $params = array(
                'email' => $id,
                'username' => $username,
                'password' => $saltedPass,
                'password_salt' => $randomSalt,
                'first_name' => $fullname
            );
            $path = '/admin?' . http_build_query($params);
            $curl = new apiConnector(self::getApiRequestUrl() . $path, $dev_token);
            $curl->setVerbPost();
            $curl->addHeader('Content-type', 'application/json');
            $curl_response = $curl->exec();
            if (isset($curl_response) && $curl_response !== false) {
                $res = json_decode($curl_response, true);
                if (array_key_exists('status', $res)) {
                    $curl->close();
                    $subject = "Welcome to Hu:toma!";
                    $body = "Congrats, you’re all set! Your Hu:toma account is confirmed. Check our intro video at https://www.youtube.com/watch?v=__pO6wVvBEY, which will guide you through using the Hu:toma platform. You will also find a chat icon on every page, which should be your go-to place for support.\nThanks\n--The Hutoma team";
                    if (!self::sendMail($id, $subject, $body)) {
                        self::log_error("registration", "Could not send welcome email to " . $id);
                    }
                    return $res['status']['code'];
                }
            }
            $curl->close();
            return "unknown";
        }
    }

    /**
     * Check if user exists with their username/email given
     * $identification - Either email/username
     */
    public static function userExists($identification)
    {
        self::construct();

        $query = "CALL getUserId(:userName, :checkEmail)";
        $sql = self::$dbh->prepare($query);
        $sql->execute(array(
            ":userName" => $identification,
            ":checkEmail" => (self::$config['features']['email_login'] === true)
        ));
        $rowCount = $sql->rowCount();

        // finally fetch the additional sql row for stored proc calls
        $sql->nextRowset();

        return $rowCount == 0 ? false : true;
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
        return "eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c";

    }

    /**
     * -------------------------
     * End Extra Tools/Functions
     * -------------------------
     */

    public static function getApiRequestUrl()
    {
        $url = getenv("HUTOMA_API_URL");
        if (isset($url) && $url != "") {
            return $url;
        }
        return self::$config["api"]["request_url"];
    }

    /**
     * Any mails need to be sent by logSys goes to here
     */
    public static function sendMail($email, $subject, $body)
    {
        $headers = array();
        $headers[] = "MIME-Version: 1.0";
        $headers[] = "Content-type: text/html; charset=iso-8859-1";
        $headers[] = "From: " . self::$config['basic']['email'];
        $headers[] = "Reply-To: " . self::$config['basic']['email'];
        return mail($email, $subject, $body, implode("\r\n", $headers));
    }

    public static function inviteCodeValid($code)
    {
        self::construct();
        $dev_token = self::getAdminToken();
        $path = "/invite/" . $code;

        $curl = new apiConnector(self::getApiRequestUrl() . $path, $dev_token);
        $curl->setVerbGet();
        $curl_response = $curl->exec();

        if (isset($curl_response) && $curl_response !== false) {
            $res = json_decode($curl_response, true);
            if (array_key_exists('status', $res)) {
                $curl->close();
                return $res['status']['code'];
            }
        }

        $curl->close();
        return "unknown";
    }

    public static function redeemInviteCode($code, $username)
    {
        self::construct();
        $dev_token = self::getAdminToken();

        $params = array(
            'username' => $username
        );

        $path = "/invite/" . $code . "/redeem?" . http_build_query($params);
        $curl = new apiConnector(self::getApiRequestUrl() . $path, $dev_token);
        $curl->setVerbPost();
        $curl_response = $curl->exec();

        if (isset($curl_response) && $curl_response !== false) {
            $res = json_decode($curl_response, true);
            if (array_key_exists('status', $res)) {
                $curl->close();
                return $res['status']['code'];
            }
        }

        $curl->close();
        return "unknown";
    }

    public static function getGoogleAnalyticsTrackerObject()
    {
        $trackerObject = getenv("GOOGLE_ANALYTICS_TRACKER");
        if (isset($trackerObject) && $trackerObject != "") {
            return $trackerObject;
        }
        return null;
    }

    /**
     * A function to handle the Forgot Password process
     */
    public static function forgotPassword()
    {
        self::construct();
        $curStatus = "initial";  // The Current Status of Forgot Password process
        $identName = self::$config['features']['email_login'] === false ? "Username" : "Username / E-Mail";

        if (!isset($_POST['logSysForgotPass']) && !isset($_GET['resetPassToken']) && !isset($_POST['logSysForgotPassChange'])) {
            $html = '<form action="' . self::curPageURL() . '" method="POST">';
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
            //TODO: move this to a stored procedure - at the moment this code is not being used.
            $sql = self::$dbh->prepare("SELECT `uid` FROM `" . self::$config['db']['token_table'] . "` WHERE `token` = ?");
            $sql->execute(array($reset_pass_token));

            if ($sql->rowCount() == 0 || $reset_pass_token == "") {
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
            //TODO: move this to a stored procedure - at the moment this code is not being used.
            $sql = self::$dbh->prepare("SELECT `uid` FROM `" . self::$config['db']['token_table'] . "` WHERE `token` = ?");
            $sql->execute(array($reset_pass_token));

            if ($sql->rowCount() == 0 || $reset_pass_token == "") {
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
                    self::$user = $sql->fetchColumn();
                    self::$loggedIn = true;

                    if (self::changePassword($_POST['logSysForgotPassNewPassword'])) {
                        self::$user = false;
                        self::$loggedIn = false;

                        /**
                         * The token shall not be used again, so remove it.
                         */
                        //TODO: move this to a stored procedure - at the moment this code is not being used.
                        $sql = self::$dbh->prepare("DELETE FROM `" . self::$config['db']['token_table'] . "` WHERE `token` = ?");
                        $sql->execute(array($reset_pass_token));

                        echo "<h3>Success : Password Reset Successful</h3><p>You may now login with your new password.</p>";
                        $curStatus = "passwordChanged"; // The password was successfully changed
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
                //TODO: move this to a stored procedure - at the moment this code is not being used.

                $query = "CALL getUser(:userName, :checkEmail)";
                $sql = self::$dbh->prepare($query);
                $sql->execute(array(
                    ":userName" => $identification,
                    ":checkEmail" => (self::$config['features']['email_login'] === true)
                ));
                if ($sql->rowCount() == 0) {
                    $curStatus = "userNotFound"; // The user with the identity given was not found in the users database
                    self::log_info("reset_pwd", "User '" . $identification . "' attempted to reset password, but user not found");
                } else {
                    $rows = $sql->fetch(\PDO::FETCH_ASSOC);
                    $email = $rows['email'];
                    $uid = $rows['id'];
                    $token = self::rand_string(40);
                    $query = "CALL insertResetToken(:token, :uid)";
                    $sql = self::$dbh->prepare($query);
                    $sql->execute(array(
                        ":token" => $token,
                        ":uid" => $uid
                    ));

                    $encodedToken = urlencode($token);
                    $subject = "Hu:toma Password Reset";
                    $body = "Hello, we got a request to reset your password. If you ignore this message, your password won't be changed. If you do want to change your password please follow this link :
                      <blockquote>
                        <a href='" . self::curPageURL() . "?resetPassToken={$encodedToken}'>Reset Password : {$token}</a>
                      </blockquote><br/>Thanks!<br/>-the Hu:toma Team";
                    if (!self::sendMail($email, $subject, $body)) {
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
        return $curStatus;
    }

    /**
     * A function that handles the logged in user to change her/his password
     */
    public static function changePassword($newpass)
    {
        self::construct();
        if (self::$loggedIn) {
            $randomSalt = self::rand_string(20);
            $saltedPass = hash('sha256', $newpass . self::$config['keys']['salt'] . $randomSalt);
            //TODO: move this to a stored procedure - at the moment this code is not being used.
            $sql = self::$dbh->prepare("UPDATE `" . self::$config['db']['table'] . "` SET `password` = ?, `password_salt` = ? WHERE `id` = ?");
            $sql->execute(array($saltedPass, $randomSalt, self::$user));
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
        $created = self::getUser("created");
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
     * Fetches data of user in database. Returns a single value or an
     * array of value according to parameteres given to the function
     */
    public static function getUser($what = "*", $user = null)
    {
        self::construct();
        if ($user == null) {
            $user = self::$user;
        }

        if (is_array($what)) {
            $columns = implode("`,`", $what);
            $columns = "`{$columns}`";
        } else {
            $columns = $what != "*" ? "`$what`" : "*";
        }

        $query = "CALL getUserById(:id, :columns)";
        $sql = self::$dbh->prepare($query);

        /* Bind the values */
        $sql->bindValue(":id", $user);
        $sql->bindValue(":columns", $columns);
        $sql->execute();

        $data = $sql->fetch(\PDO::FETCH_ASSOC);

        if (!is_array($what)) {
            $data = $what == "*" ? $data : $data[$what];
        }

        // finally fetch the additional sql row for stored proc calls
        $sql->nextRowset();

        return $data;
    }

    /**
     * 2 Step Verification Login Process
     * ---------------------------------
     * When user logs in, it checks whether there is a cookie named "logSysdevice" and if there is :
     *    1. Checks `config` -> `two_step_login` -> `devices_table` table in DB whethere there is a token with value as that of $_COOKIES['logSysdevice']
     *    2. If there is a row in table, then the "Enter Received Token" form is not shown and is directly logged in if username & pass is correct
     * If there is not a cookie, then :
     *    1. The "Enter Received token" form is shown
     *    2. If the token entered is correct, then a unique string is set as $_COOKIE['logSysdevice'] value and inserted to `config` -> `two_step_login` -> `devices_table` table in DB
     *    3. The $_COOKIE['logSysdevice'] is set to be stored for 4 months
     * ---------------------
     * ^ In the above instructions, the token sending to E-Mail/SMS is not mentioned. Assume that it is done
     */
    public static function twoStepLogin($identification = "", $password = "", $remember_me = false)
    {
        if (isset($_POST['logSys_two_step_login-token']) && isset($_POST['logSys_two_step_login-uid']) && $_SESSION['logSys_two_step_login-first_step'] === '1') {
            /**
             * The user's ID and token is got through the form
             * User = One who is about to log in and is stuck at 2 step verification
             */
            $uid = $_POST['logSys_two_step_login-uid'];
            $token = $_POST['logSys_two_step_login-token'];

            //TODO: this statement needs to move to stored procedure
            $sql = self::$dbh->prepare("SELECT COUNT(1) FROM `" . self::$config['db']['token_table'] . "` WHERE `token` = ? AND `uid` = ?");
            $sql->execute(array($token, $uid));

            if ($sql->fetchColumn() == 0) {
                /**
                 * To prevent user from Brute Forcing the token, we set the
                 * status of the first login step to false,
                 * so that the user would have to login again
                 */
                $_SESSION['logSys_two_step_login-first_step'] = '0';
                echo "<h3>Error : Wrong/Invalid Token</h3>";
                return "invalidToken";
            } else {
                /**
                 * Register User's new device if and only if
                 * the user wants to remember the device from
                 * which the user is logging in
                 */
                if (isset($_POST['logSys_two_step_login-dontask'])) {
                    $device_token = self::rand_string(10);
                    //TODO: this statement needs to move to stored procedure
                    $sql = self::$dbh->prepare("INSERT INTO `" . self::$config['two_step_login']['devices_table'] . "` (`uid`, `token`, `last_access`) VALUES (?, ?, NOW())");
                    $sql->execute(array($uid, $device_token));
                    setcookie("logSysdevice", $device_token, strtotime(self::$config['two_step_login']['expire']), self::$config['cookies']['path'], self::$config['cookies']['domain']);
                }

                /**
                 * Revoke token from reusing
                 */
                //TODO: this statement needs to move to stored procedure
                $sql = self::$dbh->prepare("DELETE FROM `" . self::$config['db']['token_table'] . "` WHERE `token` = ? AND `uid` = ?");
                $sql->execute(array($token, $uid));
                self::login(self::getUser("username", $uid), "", isset($_POST['logSys_two_step_login-remember_me']));
            }
            return true;
        } else if ($identification != "" && $password != "") {
            $login = self::login($identification, $password, $remember_me, false);
            if ($login === false) {
                /**
                 * Username/Password wrong
                 */
                return false;
            } else if (is_array($login) && $login['status'] == "blocked") {
                return $login;
            } else {
                /**
                 * Get the user ID from \Fr\LS::login()
                 */
                $uid = $login;

                /**
                 * Check if device is verfied so that 2 Step Verification can be skipped
                 */
                if (isset($_COOKIE['logSysdevice'])) {
                    //TODO: move this to a stored procedure - at the moment this code is not being used.
                    $sql = self::$dbh->prepare("SELECT 1 FROM `" . self::$config['two_step_login']['devices_table'] . "` WHERE `uid` = ? AND `token` = ?");
                    $sql->execute(array($uid, $_COOKIE['logSysdevice']));
                    if ($sql->fetchColumn() == "1") {
                        $verfied = true;
                        /**
                         * Update last accessed time
                         */
                        //TODO: this statement needs to move to stored procedure
                        $sql = self::$dbh->prepare("UPDATE `" . self::$config['two_step_login']['devices_table'] . "` SET `last_access` = NOW() WHERE `uid` = ? AND `token` = ?");
                        $sql->execute(array($uid, $_COOKIE['logSysdevice']));

                        self::login(self::getUser("username", $uid), "", $remember_me);
                        return true;
                    }
                }
                /**
                 * Start the 2 Step Verification Process
                 * Do only if callback is present and if
                 * the device is not verified
                 */
                if (is_callable(self::$config['two_step_login']['send_callback']) && !isset($verified)) {
                    /**
                     * The first part of 2 Step Login is completed
                     */
                    $_SESSION['logSys_two_step_login-first_step'] = '1';

                    /**
                     * The 2nd parameter depends on `config` -> `two_step_login` -> `numeric`
                     */
                    $token = self::rand_string(self::$config['two_step_login']['token_length'], self::$config['two_step_login']['numeric']);

                    /**
                     * Save the token in DB
                     */
                    //TODO: this statement needs to move to stored procedure
                    $sql = self::$dbh->prepare("INSERT INTO `" . self::$config['db']['token_table'] . "` (`token`, `uid`, `requested`) VALUES (?, ?, NOW())");
                    $sql->execute(array($token, $uid));

                    call_user_func_array(self::$config['two_step_login']['send_callback'], array($uid, $token));

                    /**
                     * Display the form
                     */
                    $html = "<form action='" . self::curPageURL() . "' method='POST'>
            <p>" . self::$config['two_step_login']['instruction'] . "</p>
            <label>
              <p>Token Received</p>
              <input type='text' name='logSys_two_step_login-token' placeholder='Paste the token here... (case sensitive)' />
            </label>
            <label style='display: block;'>
              <span>Remember this device ?</span>
              <input type='checkbox' name='logSys_two_step_login-dontask' />
            </label>
            <input type='hidden' name='logSys_two_step_login-uid' value='" . $uid . "' />
            " . ($remember_me === true ? "<input type='hidden' name='logSys_two_step_login-remember_me' />" : "") . "
            <label>
              <button>Verify</button>
            </label>
          </form>";
                    echo $html;
                    return "formDisplay";
                } else {
                    self::log_error("2step_login", "two_step_login: Token Callback not present");
                }
            }
        }
        /**
         * 2 Step Login is not doing any actions or
         * hasn't returned anything before. If so,
         * then return false to indicate that the
         * function is not doing anything
         */
        return false;
    }

    /**
     * A function to login the user with the username and password.
     * As of version 0.4, it is required to include the remember_me parameter
     * when calling this function to avail the "Remember Me" feature.
     */
    public static function login($username, $password, $remember_me = false, $cookies = true)
    {
        self::construct("login");

        if (self::$db === true) {
            /**
             * We Add LIMIT to 1 in SQL query because to
             * get an array with key as the column name.
             */
            $query = "CALL getUser(:userName, :checkEmail)";
            $sql = self::$dbh->prepare($query);
            $sql->execute(array(
                ":userName" => $username,
                ":checkEmail" => (self::$config['features']['email_login'] === true)
            ));

            if ($sql->rowCount() == 0) {
                // finally fetch the additional sql row for stored proc calls
                $sql->nextRowset();

                // No such user like that
                return false;
            } else {
                /**
                 * Get the user details
                 */
                $rows = $sql->fetch(\PDO::FETCH_ASSOC);

                // finally fetch the additional sql row for stored proc calls
                $sql->nextRowset();

                $us_id = $rows['id'];
                $us_pass = $rows['password'];
                $us_salt = $rows['password_salt'];
                $status = $rows['attempt'];
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
                        self::updateUserLoginAttempts("" // No tries at all
                            , $us_id);
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
                    self::log_info("login", "UserId: " . $us_id);
                    if ($cookies === true) {

                        $_SESSION['logSyscuruser'] = $us_id;

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
                            self::updateUserLoginAttempts("0",
                                $us_id);
                        }

                        // Store the dev token
                        $devToken = $rows['dev_token'];
                        self::$sessionObj->setDevToken($devToken);

                        // Redirect
                        if (self::$init_called) {
                            self::redirect(self::$config['pages']['home_page']);
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
                            self::updateUserLoginAttempts("1", // Tried 1 time
                                $us_id);
                        } else if ($status == $max_tries) {
                            /**
                             * Account Blocked. User will be only able to
                             * re-login at the time in UNIX timestamp
                             */
                            $eligible_for_next_login_time = strtotime("+" . self::$config['brute_force']['time_limit'] . " seconds", time());
                            self::updateUserLoginAttempts("b-" . $eligible_for_next_login_time,
                                $us_id);
                            return array(
                                "status" => "blocked",
                                "minutes" => round(abs($eligible_for_next_login_time - time()) / 60, 0),
                                "seconds" => round(abs($eligible_for_next_login_time - time()) / 60 * 60, 2)
                            );
                        } else if ($status < $max_tries) {
                            // If the attempts are less than Max and not Max
                            self::updateUserLoginAttempts($status + 1, // Increase the no of tries by +1.
                                $us_id);
                        }
                    }
                    return false;
                }
            }
        }
    }

    /**
     * Updates the login attempts of the user
     */
    public static function updateUserLoginAttempts($attempts, $user = null)
    {
        self::construct();
        if ($user == null) {
            $user = self::$user;
        }

        $sql = self::$dbh->prepare("CALL updateUserLoginAttempts(:id, :attempts)");
        $sql->bindValue(":id", $user);
        $sql->bindValue(":attempts", $attempts);

        $sql->execute();

        // finally fetch the additional sql row for stored proc calls
        $sql->nextRowset();
    }

    /**
     * Check if E-Mail is valid
     */
    public static function validEmail($email = "")
    {
        return filter_var($email, FILTER_VALIDATE_EMAIL);
    }

    /**
     * ---------------------
     * NEW CONSOLE FUNCTIONS
     * ---------------------
     */


    public static function getDevToken()
    {
        if (self::$sessionObj->getDevToken() === NULL) {
            $token = "";
            if (self::$loggedIn) {
                $query = "CALL getDevToken(:id)";
                $sql = self::$dbh->prepare($query);
                $sql->execute(array(
                    ":id" => self::$user
                ));
                if ($sql->rowCount() > 0) {
                    $rows = $sql->fetch(\PDO::FETCH_ASSOC);
                    $sql->nextRowset();
                    $token = $rows['dev_token'];
                } else {
                    telemetry::getInstance()->log(TelemetryEvent::ERROR, "getDevToken", "Could not obtain dev token");
                }
            }
            self::$sessionObj->setDevToken($token);
        }
        return self::$sessionObj->getDevToken();
    }

    public static function isLoggedIn()
    {
        return self::$loggedIn;
    }

    public static function isSessionActive()
    {
        if (self::$sessionObj->getLastActivity() != null && (time() - self::$sessionObj->getLastActivity() > 1800)) {
            // last request was more than 30 minutes ago
            session_unset();     // unset $_SESSION variable
            session_destroy();   // destroy session
        }
        self::$sessionObj->setLastActivity(time());
        $sid = session_id();
        return function_exists('session_status') ? (PHP_SESSION_ACTIVE == session_status()) : (!empty($sid));
    }

    function debug($data)
    {
        echo 'console.log(' . $data . ');';
    }


}

?>