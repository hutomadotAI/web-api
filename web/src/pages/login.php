<?php

require "config.php";


if(isset($_POST['action_login'])){
    $identification = $_POST['login'];
    $password = $_POST['password'];
    $redirect = $_POST['redirect'];

    $loginerror  ='<div class="alert alert-danger text-white flat">';
    $loginerror .='<i class="icon fa fa-warning"></i> The username or password you entered is incorrect';
    $loginerror .='</div>';

    if($identification == "" || $password == ""){
        $msg = array("Error", $loginerror);
    }else{
        try {
            $login = \hutoma\console::login($identification, $password, isset($_POST['remember_me']), true, $redirect);
            if ($login === false) {
                $msg = array("Error", $loginerror);
            } else if (is_array($login) && $login['status'] == "blocked") {
                $msg = array("Error", "Too many login attempts. You can try again after " . $login['minutes'] . " minutes (" . $login['seconds'] . " seconds)");
            }

            $redirectPage = $_GET["redirect"];
            if (isset($redirectPage)) {
                \hutoma\utils::redirect(urldecode($redirectPage));
            }
        }
        catch(Exception $e){
            $servererror  ='<div class="alert alert-danger text-white flat">';
            $servererror .='<i class="icon fa fa-warning"></i> Server connection lost';
            $servererror .='</div>';
            $msg = array("Error", $servererror);
        }
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
    <title>hutoma | login</title>
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
    
    <link rel="stylesheet" href="https://www.hutoma.com/css/main.css">

    <script type="text/javascript" src="../console/scripts/external/jQuery/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="../console/scripts/cookiePolicyBar/cookiePolicyBar.js"></script>
    <script type="text/javascript" src="../console/scripts/external/iCheck/icheck.min.js"></script>

    <script type="text/javascript">
        var options = {
            declineButtonText: '',
            policyUrl: 'https://www.hutoma.com/privacy.pdf',
            policyUrlTarget: '_blank'
        };
        $(document).ready(function () {
            $.cookiePolicyBar(options);
        });
    </script>
    <style>
        html {
            position: relative;
            min-height: 100%;
        }
    </style>
</head>
<body class="web-body" id="body">
<?php include_once "../console/common/google_analytics.php"; ?>
<?php include_once "./header.php"; ?>

<section>
    <div class="login-box">
        <div class="login-box-body" style="border: 1px solid #d2d6de; background-color: #202020;">
            <p class="login-box-msg"><b>sign in and start creating awesomeness</b></p>

            <form action="login.php" method="POST">
                <?php
                if (isset($msg)) {
                    echo "$msg[1]";
                }
                if (isset($_REQUEST['redirect'])) {
                    echo '<input type="hidden" name="redirect" value="' . $_REQUEST['redirect'] . '">';
                }
                ?>
                <div class="form-group has-feedback">
                    <input name="login" type="email" class="form-control flat" placeholder="Email">
                    <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                </div>
                <div class="form-group has-feedback">
                    <input name="password" type="password" class="form-control flat" placeholder="Password">
                    <span class="glyphicon glyphicon-lock form-control-feedback"></span>
                </div>
                <div class="row">
                    <div class="col-xs-8">
                        <div class="checkbox icheck" >
                            <label>
                                <input name="remember_me" type="checkbox"> Remember Me
                            </label>
                        </div>
                    </div>
                    <!-- /.col -->
                    <div class="col-xs-4">
                        <button type="submit" name="action_login" class="btn btn-primary btn-block btn-flat">Sign In
                        </button>
                    </div>
                    <!-- /.col -->
                </div>
            </form>
            <a class="new-link" href="reset.php">I forgot my password</a><br>
            <a class="new-link" href="register.php" class="text-center">Register a new account</a>
        </div>
        <!-- /.login-box-body -->
    </div>
</section>

<?php include_once "./footer.php"; ?>

</body>
</html>
