<?php

include_once __DIR__ . "/../console/api/signupCodeApi.php";
include_once __DIR__ . "/../console/common/config.php";

include "config.php";

function getErrorMessage($message, $alertType = 'alert-warning') {
    $msg ='<div class="alert ' . $alertType . ' text-white flat">';
    $msg .='<i class="icon fa fa-exclamation"></i> ' . $message;
    $msg .='</div>';
    return $msg;
}

if(isset($_POST['submit'])) {

    if(isset($_POST['g-recaptcha-response'])) {
        $captcha=$_POST['g-recaptcha-response'];
        $response=json_decode(file_get_contents("https://www.google.com/recaptcha/api/siteverify?secret=6LfUJhMUAAAAAF_JWYab5E1oBqZ-XWtHer5n67xO&response=".$captcha."&remoteip=".$_SERVER['REMOTE_ADDR']), true);
        if($response['success'] == false)
        {
            $msg = getErrorMessage('You did not pass the captcha test');
        }
        else {
            $email = $_POST['email'];
            $password = $_POST['pass'];
            $retyped_password = $_POST['retyped_password'];
            $name = $_POST['username'];
            $terms = isset($_POST['terms']);
            $invite_code = $_POST['promo_code'];

            $passwordCompliance =
                preg_match('/\d+/', $password) == 1       // at least one digit
                && strlen($password) >= 6                         // minimum length of 6 chars
                && preg_match('/[a-z]/', $password) == 1  // at least a lowercase letter
                && preg_match('/[A-Z]/', $password) == 1; // at least an uppercase letter

            $api = new \hutoma\api\signupCodeApi(\hutoma\console::isLoggedIn(), \hutoma\config::getAdminToken());
            if( $email == "" || $password == '' || $retyped_password == '' || $name == '' ) {
                $msg= getErrorMessage('Some fields were left blank.');
            }
            elseif (!$passwordCompliance) {
                $msg = getErrorMessage('The password needs to be at least 6 characters long, contain at least a digit and both uppercase and lowercase letters');
            }
            elseif($password != $retyped_password) {
                $msg= getErrorMessage('The passwords you entered do not match.');
            }
            elseif($terms != 'True') {
                $msg= getErrorMessage('Please indicate that you have read the Hu:toma Subscription Agreement thoroughly and agree to the terms stated.');
            }
            elseif($invite_code !== '' && $api->inviteCodeValid($invite_code) !== 200) {
                $msg  = getErrorMessage('Your promo code is invalid, please try again.');
            } else {
                $createAccount = \hutoma\console::register($email, $password, $email, $name, date("Y-m-d H:i:s"));

                if($createAccount === "exists") {
                    $msg = getErrorMessage('This user already exists.');
                } elseif ($createAccount === "unknown") {
                    $msg = getErrorMessage('Unspecified error code.', 'alert-danger');
                } else {
                    // Register succeeded
                    if ($createAccount === 200) {
                        // Redeem invite code.

                        if ($invite_code !== null) {
                            $api->redeemInviteCode($invite_code, $email);
                        }

                        setcookie('logSyscuruser', $email);

                        // Try to login if successful redirect to homepage using naive register trigger
                        $login = \hutoma\console::login(
                            $email, 
                            $password, 
                            false, 
                            true, 
                            \hutoma\console::$config['pages']['home_page'] . '?register=1'
                        );

                        if ($login === false) {
                            $msg = getErrorMessage('There was an error creating the user - please try again later.');
                        } elseif (is_array($login) && $login['status'] == "blocked") {
                            $msg = getErrorMessage('Too many login attempts. You can try again after ' . $login['minutes'] . ' minutes (' . $login['seconds'] . ' seconds)');
                            exit();
                        }
                    } else {
                        // Registration threw an error
                        $msg = getErrorMessage("Error code:" . $createAccount);
                    }
                }
            }

            unset($api);

        }
    }
    else
    {
        $msg  = getErrorMessage('Please check the captcha checkbox');
    }


}

?>

<!DOCTYPE html>
<!--[if lt IE 7]>
<html lang="en" class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>
<html lang="en" class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>
<html lang="en" class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html lang="en" class="no-js"> <!--<![endif]-->

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Hu:toma | Register</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="keywords" content="Deep learning, AI, Hutoma, Artificial Intelligence, Machine Learning, Siri, Cortana, Deep Learning API, AI Marketplace, Chatbots">
    <meta name="description" content="Hu:toma helps developers around the world build and monetize deep learning chatbots by providing free access to a proprietary platform offering both the tools and the channels to create and share conversational AIs.">
    <meta name="author" content="hutoma limited">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="../console/dist/css/hutoma.css">
    <link rel="stylesheet" href="../console/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../console/dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="../console/scripts/cookiePolicyBar/cookiePolicyBar.css">

    <link rel="stylesheet" href="https://www.hutoma.ai/css/main.css">
    <link rel="icon" href="../console/dist/img/favicon.ico" type="image/x-icon">

    <script type="text/javascript" src="../console/scripts/external/jQuery/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="../console/scripts/cookiePolicyBar/cookiePolicyBar.js"></script>
    <script type="text/javascript" src="../console/scripts/external/iCheck/icheck.min.js"></script>
    
    <script src='../console/scripts/security/password.js'></script>
    <script src='https://www.google.com/recaptcha/api.js'></script>
    
    <script type="text/javascript">
        var options = {
            declineButtonText: '',
            policyUrl: 'privacyPolicy.php',
            policyUrlTarget: '_blank'
        };
        $(document).ready(function () {
            $.cookiePolicyBar(options);
        });
    </script>
    <style>
        /* Sticky footer styles
        -------------------------------------------------- */
        html {
            position: relative;
            min-height: 100%;
        }
        
        .progress {
            height: 11px;
        }

        .progress-hide {
            visibility: hidden;
        }

        .progress-bar-text {
            color: white;
            text-align: right;
            width: 100%;
            position: relative;
            top: -5px;
        }

        .form-control-error {
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075), 0 0 8px rgba(255, 100, 100, .6) !important;
        }

    </style>
    <?php include_once "../console/common/google_tag_manager.php" ?>
</head>
<body id="body" class="web-body hold-transition register-page">
    <?php include_once "../console/common/google_tag_manager_no_js.php" ?>
    <?php include_once "./header.php"; ?>

<section>
    <div class="register-box">
    <div class="register-box-body" style="background-color: #202020;">
        <p class="login-box-msg"><b>register a new account</b></p>
        <form action="register.php" method="POST">
            <?php if(isset($msg)){echo $msg;}?>
            <div class="form-group has-feedback">
                <input name="username" type="text" class="form-control flat" placeholder="Full name" value="<?php if (isset($_POST['username'])) echo $_POST['username']?>">
                <span class="glyphicon glyphicon-user form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="email"  type="email" class="form-control flat" placeholder="Email" value="<?php if (isset($_POST['email'])) echo $_POST['email']?>">
                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback no-margin">
                <input id="passwordField" name="pass"  type="password" class="form-control flat" placeholder="Password" onkeyup="passwordStrength(this.value)">
                <span class="glyphicon glyphicon-lock form-control-feedback"></span>
                <div style="margin-top:2px;margin-bottom:2px;">
                    <div class="progress active no-margin flat progress-hide" id="progress_strength" style="background-color: #343434;">
                        <div id="pstrength" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
                            <span id="progress-bar-text" class="progress-bar-text"></span>
                        </div>
                    </div>
                </div>

            </div>
            <div class="form-group has-feedback">
                <input id="passConfirmationField" name="retyped_password"  type="password" class="form-control flat" placeholder="Retype password"
                       onkeyup="confirmPassword('passwordField','passConfirmationField');">
                <span class="glyphicon glyphicon-log-in form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="promo_code"  type="text" class="form-control flat" placeholder="Promo Code (Optional)" value="<?php if (isset($_GET['code'])) echo $_GET['code']?>">
                <span class="glyphicon glyphicon-barcode form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="col-xs-8">
                    <div class="checkbox icheck">
                        <label>
                            <input name="terms" type="checkbox" <?php if (isset($_POST['terms']) ) echo 'checked'?> > I agree to the terms stated in the Subscription <a href="https://www.hutoma.ai/Hutoma_WebPlatformSaaSAgreement.pdf" target="_blank">Agreement.</a>.
                        </label>
                        </label>
                    </div>
                </div><!-- /.col -->

                <div class="col-xs-4">
                    <button type="submit" name="submit" class="btn btn-primary btn-block btn-flat">Register</button>
                </div><!-- /.col -->
            </div>
            <br/>
            <div class="col-xs-12" style="padding: 5px 15px 15px 10px;">
                <div class="g-recaptcha" data-sitekey="6LfUJhMUAAAAAJEn_XfTOR6tOeyecWX6o6i9jqiW"></div>
            </div>
        </form>
        <a href="login.php" class="text-center new-link">I already have an account</a><br/>
    </div><!-- /.form-box -->
    </div><!-- /.register-box -->
</section>

<?php include_once "./footer.php"; ?>
<!-- Google Code for sign-up Conversion Page -->
<script type="text/javascript">
    /* <![CDATA[ */
    var google_conversion_id = 843069449;
    var google_conversion_language = "en";
    var google_conversion_format = "3";
    var google_conversion_color = "ffffff";
    var google_conversion_label = "LtRjCIPTx3MQifCAkgM";
    var google_remarketing_only = false;
    /* ]]> */
</script>
<script type="text/javascript" src="//www.googleadservices.com/pagead/conversion.js">
</script>
<noscript>
    <div style="display:inline;">
        <img height="1" width="1" style="border-style:none;" alt="" src="//www.googleadservices.com/pagead/conversion/843069449/?label=LtRjCIPTx3MQifCAkgM&amp;guid=ON&amp;script=0"/>
    </div>
</noscript>

</body>
</html>
