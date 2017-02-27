<?php
include "config.php";

if(isset($_POST['submit'])) {

    if(isset($_POST['g-recaptcha-response'])) {
        $captcha=$_POST['g-recaptcha-response'];
        $response=json_decode(file_get_contents("https://www.google.com/recaptcha/api/siteverify?secret=6LfUJhMUAAAAAF_JWYab5E1oBqZ-XWtHer5n67xO&response=".$captcha."&remoteip=".$_SERVER['REMOTE_ADDR']), true);
        if($response['success'] == false)
        {
            $msg ='<div class="alert alert-warning text-white flat">';
            $msg .='<i class="icon fa fa-exclamation"></i> You did not pass the captcha test';
            $msg .='</div>';
        }
        else {
            $email = $_POST['email'];
            $password = $_POST['pass'];
            $retyped_password = $_POST['retyped_password'];
            $name = $_POST['username'];
            $terms = isset($_POST['terms']);
            $invite_code =$_POST['invite_code'];

            $missingfields  ='<div class="alert alert-warning text-white flat">';
            $missingfields .='<i class="icon fa fa-exclamation"></i> Some fields were left blank.';
            $missingfields .='</div>';

            $passwordmismatch  ='<div class="alert alert-warning text-white flat">';
            $passwordmismatch .='<i class="icon fa fa-exclamation"></i> The passwords you entered do not match.';
            $passwordmismatch .='</div>';

            $termsmsg  ='<div class="alert alert-warning text-white flat">';
            $termsmsg .='<i class="icon fa fa-exclamation"></i> Please indicate that you have read and agree to the <a href="https://www.hutoma.com/terms_and_conditions.pdf" target="_blank">Terms and Conditions</a>,<a href="https://www.hutoma.com/subscription_agreement.pdf" target="_blank"> Platform Usage</a> and <a href="https://www.hutoma.com/privacy.pdf" target="_blank">Privacy Policy</a>';
            $termsmsg .='</div>';

            $userexists  ='<div class="alert alert-warning text-white flat">';
            $userexists .='<i class="icon fa fa-exclamation"></i> This user already exists.';
            $userexists .='</div>';

            $invalidcode  ='<div class="alert alert-warning text-white flat">';
            $invalidcode .='<i class="icon fa fa-exclamation"></i> Please enter a valid invitation code.</a>';
            $invalidcode .='</div>';


            $msg= $missingfields;

            if( $email == "" || $password == '' || $retyped_password == '' || $name == '' ) $msg= $missingfields;
            elseif($password != $retyped_password) $msg= $passwordmismatch;
            elseif($terms != 'True') $msg= $termsmsg;
            elseif(\hutoma\console::inviteCodeValid($invite_code) !== 200) $msg=$invalidcode;
            else{
                $createAccount = \hutoma\console::register($email, $password, $email, $name, date("Y-m-d H:i:s"));

                if($createAccount === "exists") {
                    $msg= $userexists;
                } elseif ($createAccount === "unknown") {
                    $msg = array("Error", "Unspecified error code");
                } else {
                    // Register succeeded
                    if ($createAccount === 200) {
                        // Redeem invite code.
                        \hutoma\console::redeemInviteCode($invite_code, $email);

                        setcookie('logSyscuruser', $email);
                        $login = \hutoma\console::login($email, $password, false);
                        if ($login === false) {
                            $msg = array("Error", $loginerror);
                        } elseif (is_array($login) && $login['status'] == "blocked") {
                            $msg = array("Error", "Too many login attempts. You can try again after " . $login['minutes'] . " minutes (" . $login['seconds'] . " seconds)");
                            exit();
                        }
                    } else {
                        // Registration threw an error
                        $msg = array("Error", "Error code:" . $createAccount);
                    }
                }
            }

        }
    }
    else
    {
        $msg  ='<div class="alert alert-warning text-white flat">';
        $msg.='<i class="icon fa fa-exclamation"></i> Please check the captcha checkbox';
        $msg .='</div>';
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
    <title>Hu:toma | New Account </title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="keywords" content="Deep learning, AI, Hutoma, Artificial Intelligence, Machine Learning, Siri, Cortana, Deep Learning API, AI Marketplace, Chatbots">
    <meta name="description" content="Hu:toma helps developers around the world build and monetize deep learning chatbots by providing free access to a proprietary platform offering both the tools and the channels to create and share conversational AIs.">
    <meta name="author" content="hutoma limited">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="../console/dist/css/hutoma.css">
    <link rel="stylesheet" href="../console/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../console/dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="../console/plugins/cookiePolicyBar/cookiePolicyBar.css">

    <link rel="stylesheet" href="https://www.hutoma.com/css/main.css">

    <script type="text/javascript" src="../console/plugins/jQuery/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="../console/plugins/cookiePolicyBar/cookiePolicyBar.js"></script>
    <script type="text/javascript" src="../console/plugins/iCheck/icheck.min.js"></script>
    
    <script src='../console/plugins/security/password.js'></script>
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
        .newa {
            color: #3c8dbc;
        }
        .newa:hover,
        .newa:active,
        .newa:focus {
            outline: none;
            text-decoration: none;
            color: #72afd2;
        }

        /* Sticky footer styles
        -------------------------------------------------- */
        html {
            position: relative;
            min-height: 100%;
        }

        body {
            background: #d2d6de;
            /* Margin bottom by footer height */
            margin-bottom: 350px;
            font-family: 'Muli', 'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif;

        }
        .footer {
            position: absolute;
            bottom: 0;
            width: 100%;
            /* Set the fixed height of the footer here */
            height: 350px;

        }

        .af {
            color: #3c8dbc;
            font-weight: bold;
        }

        .af:hover,
        .af:active,
        .af:focus {
            outline: none;
            text-decoration: none;
            color: white;
        }

        .container {
            width: auto;
            max-width: 100%;
            padding: 0 15px;
        }
        .container .text-muted {
            margin: 20px 0;
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
</head>
<body id="body" class="hold-transition register-page">
<?php include_once "../console/common/google_analytics.php"; ?>
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
                <input name="invite_code"  type="invite_code" class="form-control flat" placeholder="Invitation Code" value="<?php if (isset($_POST['invite_code'])) echo $_POST['invite_code']?>">
                <span class="glyphicon glyphicon-barcode form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="col-xs-8">
                    <div class="checkbox icheck">
                        <label>
                            <input name="terms" type="checkbox" <?php if (isset($_POST['terms']) ) echo 'checked'?> > I agree to hutoma <a href="https://www.hutoma.com/terms_and_conditions.pdf" target="_blank">terms</a>, <a href="https://www.hutoma.com/subscription_agreement.pdf" target="_blank">usage</a>, and <a href="https://www.hutoma.com/privacy.pdf" target="_blank">privacy</a> policies.
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

        <!--    <div class="social-auth-links text-center">
             <p>- OR -</p>
             <a href="#" class="btn btn-block btn-social btn-facebook btn-flat"><i class="fa fa-facebook"></i> Sign up using Facebook</a>
             <a href="#" class="btn btn-block btn-social btn-google btn-flat"><i class="fa fa-google-plus"></i> Sign up using Google+</a>
           </div>
    -->
        <a href="login.php" class="text-center newa">I already have an account</a><br/>
        <a href="https://www.hutoma.com" class="text-center newa">I need an invitation code</a>
    </div><!-- /.form-box -->
    </div><!-- /.register-box -->
</section>

<?php include_once "./footer.php"; ?>

</body>
</html>
