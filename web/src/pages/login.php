<?php
require "config.php";
if(isset($_POST['action_login'])){
    $identification = $_POST['login'];
    $password = $_POST['password'];

    $loginerror  ='<div class="alert alert-danger">';
    $loginerror .='<i class="icon fa fa-warning"></i> The username or password you entered is incorrect';
    $loginerror .='</div>';

    if($identification == "" || $password == ""){
        $msg = array("Error", $loginerror);
    }else{
        try {
            $login = \hutoma\console::login($identification, $password, isset($_POST['remember_me']));
            if ($login === false) {
                $msg = array("Error", $loginerror);
            } else if (is_array($login) && $login['status'] == "blocked") {
                $msg = array("Error", "Too many login attempts. You can try again after " . $login['minutes'] . " minutes (" . $login['seconds'] . " seconds)");
            }
        }
        catch(Exception $e){
            $servererror  ='<div class="alert alert-danger">';
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
    <meta name="keywords"
          content="Deep learning, AI, Hutoma, Artificial Intelligence, Machine Learning, Siri, Cortana, Deep Learning API">
    <meta name="description"
          content="Hutoma builds emotionally evolved AIs and Digital Employees that can have intelligent conversations with you or your customers.">
    <meta name="author" content="hutoma limited">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="../console/dist/css/hutoma.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link rel="stylesheet" href="../console/plugins/cookiePolicyBar/cookiePolicyBar.css">
    <link rel="stylesheet" href="https://www.hutoma.com/css/main.css">
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="../console/plugins/cookiePolicyBar/cookiePolicyBar.js"></script>
    <script type="text/javascript" src="https://cdn.jsdelivr.net/icheck/1.0.2/icheck.min.js"></script>

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


    </style>
</head>
<body id="body">
<?php include_once "../console/common/google_analytics.php"; ?>
<?php include_once "./header.php"; ?>

<section>
    <div
    <div class="login-box">
        <div class="login-box-body" style="border: 1px solid #d2d6de; background-color: #202020;">
            <p class="login-box-msg"><b>sign in and start creating awesomeness</b></p>

            <form action="login.php" method="POST">
                <?php if(isset($msg)){echo "$msg[1]";}?>
                <div class="form-group has-feedback">
                    <input name="login" type="email" class="form-control" placeholder="Email">
                    <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                </div>
                <div class="form-group has-feedback">
                    <input name="password" type="password" class="form-control" placeholder="Password">
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

            <!--   <div class="social-auth-links text-center">
                 <p>- OR -</p>
                 <a href="#" class="btn btn-block btn-social btn-facebook btn-flat"><i class="fa fa-facebook"></i> Sign in using Facebook</a>
                 <a href="#" class="btn btn-block btn-social btn-google btn-flat"><i class="fa fa-google-plus"></i> Sign in using Google+</a>
               </div>--><!-- /.social-auth-links -->

            <a class="newa" href="reset.php">I forgot my password</a><br>
            <a class="newa" href="register.php" class="text-center">Register a new account</a>

        </div>
        <!-- /.login-box-body -->
    </div>
</section>

<?php include_once "./footer.php"; ?>

</body>
</html>
