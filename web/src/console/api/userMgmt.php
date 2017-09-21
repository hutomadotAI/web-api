<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 09/08/17
 * Time: 15:07
 */

namespace hutoma\api;

require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/config.php";
require_once __DIR__ . "/../common/logging.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../common/emailUtil.php";
require_once __DIR__ . "/../api/adminApi.php";

use hutoma as base;

class userMgmt
{

    public static function login($username, $password, $remember_me = false, $cookies = true, $redirect = NULL)
    {
        $api = new adminApi(base\sessionObject::isLoggedIn(), base\config::getAdminToken());
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
            $saltedPass = hash('sha256', $password . base\config::getSalt() . $us_salt);
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
                base\logging::info("UserId: " . $userId);
                if ($cookies === true) {

                    $_SESSION['logSyscuruser'] = $us_id;
                    $_COOKIE['logSyscuruser'] = $userInfo['username'];
                    setcookie("logSyslogin",
                        hash("sha256",
                            base\config::getCookie() . $us_id . base\config::getCookie()),
                        strtotime(base\config::getCookieParams()['expire']),
                        base\config::getCookieParams()['path'],
                        base\config::getCookieParams()['domain']);

                    $_SESSION['navigation_id'] = $_COOKIE['logSyscuruser'];

                    if ($remember_me === true && base\config::getRememberMeFlag()) {
                        setcookie("logSysrememberMe", $us_id,
                            strtotime(base\config::getCookieParams()['expire']),
                            base\config::getCookieParams()['path'],
                            base\config::getCookieParams()['domain']);
                    }
                    base\sessionObject::setLoggedin(true);

                    if (base\config::getBlockBruteForceFlag()) {
                        // Reset attempts
                        $api->updateUserLoginAttempts($userId, "0");
                    }

                    // Store the dev token
                    base\sessionObject::setDevToken($userInfo['dev_token']);

                    // Redirect
                    base\utils::redirect(isset($redirect) ? $redirect : base\config::getHomePageUrl());
                    return true;
                } else {

                    base\logging::info("no_cookies - UserId: " . $us_id);
                    return $us_id;
                }
            } else {
                /**
                 * Incorrect password
                 * ------------------
                 * Check if brute force protection is enabled
                 */
                if (base\config::getBlockBruteForceFlag()) {
                    $max_tries = base\config::getBruteForceMaxTries();

                    if ($status == "") {
                        // User was not logged in before
                        $api->updateUserLoginAttempts($userId, "1");
                    } else if ($status == $max_tries) {
                        /**
                         * Account Blocked. User will be only able to
                         * re-login at the time in UNIX timestamp
                         */
                        $eligible_for_next_login_time = strtotime("+" . base\config::getBruteForceTimeLimit() . " seconds", time());
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

    public static function logout()
    {
        base\logging::info("logout - UserId: " . $_SESSION['logSyscuruser']);
        session_destroy();
        setcookie("logSyslogin", "", time() - 10, base\config::getCookieParams()['path'], base\config::getCookieParams()['domain']);
        setcookie("logSysrememberMe", "", time() - 10, base\config::getCookieParams()['path'], base\config::getCookieParams()['domain']);

        base\sessionObject::clear();
        /**
         * Wait for the cookies to be removed, then redirect
         */
        usleep(2000);
        base\utils::redirect(base\config::getLoginPageUrl());
        return true;
    }

    public static function changePassword($newpass)
    {
        if (base\sessionObject::isLoggedIn()) {
            $randomSalt = base\utils::getRandomSalt();
            $saltedPass = base\utils::generatePassword($newpass, $randomSalt);
            $api = new adminApi(base\sessionObject::isLoggedIn(), base\config::getAdminToken());
            $api->updateUserPassword(
                base\sessionObject::getCurrentUsername(),
                $saltedPass,
                $randomSalt
            );
            return true;
        } else {
            echo "<h3>Error : Not Logged In</h3>";
            return "notLoggedIn";
        }
    }

    public static function forgotPassword()
    {
        $curStatus = "initial";  // The Current Status of Forgot Password process
        $api = new adminApi(base\sessionObject::isLoggedIn(), base\config::getAdminToken());

        if (!isset($_POST['logSysForgotPass']) && !isset($_GET['resetPassToken']) && !isset($_POST['logSysForgotPassChange'])) {
            $html = '<form action="' . base\utils::curPageURL() . '" method="POST">';
            $html .= "<div class='form-group has-feedback'>";
            $html .= "<input  type='email' class='form-control flat' id='logSysIdentification' placeholder='enter your email'  name='identification' />";
            $html .= "<span class='glyphicon glyphicon-envelope form-control-feedback'></span>";
            $html .= "</div>";
            $html .= '<div class="form-group " style="padding: 5px 15px 15px 10px;">';
            $html .= '<div class="g-recaptcha" data-sitekey="6LfUJhMUAAAAAJEn_XfTOR6tOeyecWX6o6i9jqiW"></div></div>';
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
                echo "<h3>Error</h3> Wrong/Invalid Token";
                $curStatus = "invalidToken"; // The token user gave was not valid
            } else {
                /**
                 * The token is valid, display the new password form
                 */
                $html = "<p>The reset token was authorized. You can now proceed with changing your password</p>";
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
                echo "<h3>Error</h3>Wrong/Invalid Token";
                $curStatus = "invalidToken"; // The token user gave was not valid
            } else {
                $newPassword = $_POST['logSysForgotPassNewPassword'];
                $newRetypedPassword = $_POST['logSysForgotPassRetypedPassword'];
                if ($newPassword == "" || $newRetypedPassword == "") {
                    echo "<h3>Error</h3>Passwords Fields Left Blank";
                    $curStatus = "fieldsLeftBlank";
                } elseif ($newPassword != $newRetypedPassword) {
                    echo "<h3>Error</h3>Passwords Don't Match";
                    $curStatus = "passwordDontMatch"; // The new password and retype password submitted didn't match
                } elseif (!\hutoma\utils::isPasswordComplex($newPassword)) {
                    echo "<h3>Error</h3>The password needs to be at least 6 characters long, contain at least a digit and both uppercase and lowercase letters";
                    $curStatus = "passwordDontMatch"; // The new password and retype password submitted didn't match
                } else {
                    /**
                     * We must create a fake assumption that the user is logged in to
                     * change the password as \Fr\LS::changePassword()
                     * requires the user to be logged in.
                     */
                    $user = $api->getUserIdForToken($reset_pass_token);
                    base\sessionObject::setLoggedin(true);

                    if (self::changePassword($newPassword)) {
                        base\sessionObject::setLoggedin(false);

                        /**
                         * The token shall not be used again, so remove it.
                         */
                        if (!$api->deletePasswordResetToken($reset_pass_token)) {
                            echo "<h3>Error</h3><p>There was a problem resetting your password.</p>";
                        } else {
                            echo "<h3>Password Reset Successful</h3><p>You may now login with your new password.</p>";
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

            if(isset($_POST['g-recaptcha-response'])) {
                $captcha = $_POST['g-recaptcha-response'];
                $response = json_decode(file_get_contents("https://www.google.com/recaptcha/api/siteverify?secret=6LfUJhMUAAAAAF_JWYab5E1oBqZ-XWtHer5n67xO&response=" . $captcha . "&remoteip=" . $_SERVER['REMOTE_ADDR']), true);
                if ($response['success'] == false) {
                    echo 'You did not pass the captcha test';
                    base\logging::info("reset_pwd - User '" . $identification . "' attempted to reset password, but failed the captcha challenge");
                    return $curStatus;
                }
            }


            if ($identification == "") {
                header("Location: reset.php"); /* Redirect browser */
                base\logging::info("reset_pwd - User without identification");
                exit();

            } else {
                $userInfo = $api->getUserInfo($identification);
                if ($userInfo === null) {
                    $curStatus = "userNotFound"; // The user with the identity given was not found in the users database
                    base\logging::info("reset_pwd - User '" . $identification . "' attempted to reset password, but user not found");
                } else {
                    $email = $userInfo['email'];
                    $uid = $userInfo['id'];

                    $token = base\utils::generateRandomString(40);
                    $api->insertPasswordResetToken($uid, $token);
                    $encodedToken = urlencode($token);
                    $subject = "Hu:toma Password Reset";
                    $htmlBody = "Hello, we got a request to reset your password. If you ignore this message, your password won't be changed. If you do want to change your password please follow this link :
                      <blockquote>
                        <a href='" . base\utils::curPageURL() . "?resetPassToken={$encodedToken}'>Reset Password : {$token}</a>
                      </blockquote><br/>Thanks!<br/>-the Hu:toma Team";
                    $textBody = "Hello, we got a request to reset your password. If you ignore this message, your password won't be changed. If you do want to change your password please enter this link in your browser :\n"
                        . base\utils::curPageURL() . "?resetPassToken={$encodedToken}\n\nThanks!\n-the Hu:toma Team";

                    $emailDetails = base\config::getRegistrationEmailDetails();
                    $result = base\emailUtil::sendEmail($email, $emailDetails['from'], $subject, $htmlBody, $textBody);
                    if (array_key_exists('error', $result['data'])) {
                        echo "<p>There was a problem sending the password reset e-mail. Please go back and try again.</p>";
                        base\logging::info("reset_pwd - User '" . $identification . "' attempted to reset password, but email could not be sent. Error:" . $result['data']['error']);
                        return $curStatus;
                    }

                    $curStatus = "emailSent"; // E-Mail has been sent
                    base\logging::info("reset_pwd - User '" . $identification . "' successfully requested password reset");
                }
                echo "<p>If you entered a valid username, you should be receiving shortly a password reset email in your inbox. Don't forget to check your spam folder too!</p>";
            }
        }
        unset($api);
        return $curStatus;
    }

    public static function userExists($identification)
    {
        $api = new adminApi(base\sessionObject::isLoggedIn(), base\config::getAdminToken());
        return $api->userExists($identification);
    }

    public static function register($id, $password, $username, $fullname, $created)
    {
        if (self::userExists($id)) {
            return "exists";
        } else {
            $randomSalt = base\utils::generateRandomString(20);
            $saltedPass = hash('sha256', $password . base\config::getSalt() . $randomSalt);

            $api = new adminApi(base\sessionObject::isLoggedIn(), base\config::getAdminToken());
            if ($api->register($id, $username, $saltedPass, $randomSalt, $fullname)) {
                return 200;
            }
            return "unknown";
        }
    }
}