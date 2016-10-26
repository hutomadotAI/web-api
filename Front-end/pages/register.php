<?php
include "config.php";
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
    <script src='https://www.google.com/recaptcha/api.js'></script>


    <script type="text/javascript">
        var options = {
            declineButtonText: ''
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
            height: 200px;

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
<body class="hold-transition register-page">

<?php
include_once "../console/common/google_analytics.php";
if(isset($_POST['submit'])) {

    if(isset($_POST['g-recaptcha-response'])) {
        $captcha=$_POST['g-recaptcha-response'];
        $response=json_decode(file_get_contents("https://www.google.com/recaptcha/api/siteverify?secret=6LcArBsTAAAAAMWrUUlxsiK9Cg9fJiIYroRycv_z&response=".$captcha."&remoteip=".$_SERVER['REMOTE_ADDR']), true);
        if($response['success'] == false)
        {
            $msg ='<div class="alert alert-warning">';
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

            $missingfields  ='<div class="alert alert-warning">';
            $missingfields .='<i class="icon fa fa-exclamation"></i> Some fields were left blank.';
            $missingfields .='</div>';

            $passwordmismatch  ='<div class="alert alert-warning">';
            $passwordmismatch .='<i class="icon fa fa-exclamation"></i> The passwords you entered do not match.';
            $passwordmismatch .='</div>';

            $termsmsg  ='<div class="alert alert-warning">';
            $termsmsg .='<i class="icon fa fa-exclamation"></i> Please indicate that you have read and agree to the Terms and Conditions and Privacy Policy';
            $termsmsg .='</div>';

            $userexists  ='<div class="alert alert-warning">';
            $userexists .='<i class="icon fa fa-exclamation"></i> This user already exists.';
            $userexists .='</div>';

            $invalidcode  ='<div class="alert alert-warning">';
            $invalidcode .='<i class="icon fa fa-exclamation"></i> Please enter a valid invitation code.</a>';
            $invalidcode .='</div>';


            $msg= $missingfields;

            if( $email == "" || $password == '' || $retyped_password == '' || $name == '' ) $msg= $missingfields;
            elseif($password != $retyped_password) $msg= $passwordmismatch;
            elseif($terms != 'True') $msg= $termsmsg;
            # TODO: Tech Previw Hack. Need to remove it
            elseif($invite_code!='R4d1prGQl7wJXqgj') $msg=$invalidcode;
            else{
                $createAccount = \hutoma\console::register($email, $password, $email, $name, date("Y-m-d H:i:s"));

                if($createAccount === "exists"){
                    $msg= $userexists;
                }elseif($createAccount === true){
                    setcookie('logSyscuruser',$email);
                    $login = \hutoma\console::login($email, $password, false);
                    if($login === false){
                        $msg = array("Error", $loginerror);
                    }else if(is_array($login) && $login['status'] == "blocked"){
                        $msg = array("Error", "Too many login attempts. You can try again after ". $login['minutes'] ." minutes (". $login['seconds'] ." seconds)");
                        exit();
                    }


                }
                else  $msg= $userexists;

            }

        }
    }
    else
    {
        $msg  ='<div class="alert alert-warning">';
        $msg.='<i class="icon fa fa-exclamation"></i> Please check the captcha checkbox';
        $msg .='</div>';
    }


}
?>

<header id="navigation" class="navbar-fixed-top navbar">
    <div class="container" style="font-weight: bold">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <i class="fa fa-bars fa-2x"></i>
            </button>
            <a class="navbar-brand" href="#body">
                <h1 style="padding: 5px;margin-left: 115px;" >
                    <b> hu:toma </b>
                </h1>
            </a>
        </div>
        <nav style="padding: 5px;margin-right: 115px;" class="collapse navbar-collapse navbar-right" role="navigation">
            <ul id="nav" class="nav navbar-nav">
                <li><a href="../index.html" class="external">Home</a></li>
                <li><a href="./pages/login.php" class="external">Login</a></li>
                <li><a href="../index.html" class="external">Features  </a></li>
                <li><a href="../about.html" class="external">About</a></li>
                <li><a href="../contactus.html" class="external">Contacts</a></li>
            </ul>
        </nav>
    </div>
</header>


<div class="register-box">


    <div class="register-box-body" style="background-color: #202020;">
        <p class="login-box-msg"><b>register a new account</b></p>
        <form action="register.php" method="POST">
            <?php if(isset($msg)){echo $msg;}?>
            <div class="form-group has-feedback">
                <input name="username" type="text" class="form-control" placeholder="Full name">
                <span class="glyphicon glyphicon-user form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="email"  type="email" class="form-control" placeholder="Email">
                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="pass"  type="password" class="form-control" placeholder="Password">
                <span class="glyphicon glyphicon-lock form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="retyped_password"  type="password" class="form-control" placeholder="Retype password">
                <span class="glyphicon glyphicon-log-in form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input name="invite_code"  type="invite_code" class="form-control" placeholder="Invitation Code">
                <span class="glyphicon glyphicon-barcode form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="col-xs-8">
                    <div class="checkbox icheck">
                        <label>
                            <input name="terms" type="checkbox"> I agree to the <a class="newa" href="#">terms</a>
                        </label>
                    </div>
                </div><!-- /.col -->

                <div class="col-xs-4">
                    <button type="submit" name="submit" class="btn btn-primary btn-block btn-flat">Register</button>
                </div><!-- /.col -->
            </div>
            <br/>
            <div class="g-recaptcha" data-sitekey="6LcArBsTAAAAADPS78hYLKb05FNfwY0cMQBJZLAV"></div>



            <!--   <div class="g-recaptcha" data-sitekey="6LcArBsTAAAAADPS78hYLKb05FNfwY0cMQBJZLAV"></div> -->

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




<footer id="footer" class="footer">
    <div class="container" style="font-weight: bold;">
        <div class="row row-centered">

            <div class="col-md-3 col-sm-3 col-s-3 col-centered wow fadeInUp animated" data-wow-duration="250ms"
                 data-wow-delay="200ms">
                <div class="footer-single">
                    <h6>About</h6>
                    <ul>
                        <li><a class="af" href="../about.html">About Hutoma</a></li>
                        <li><a class="af" href="../pricing.html">Pricing</a></li>
                        <li><a class="af" href="../contactus.html">Contact Us</a></li>

                    </ul>
                </div>
            </div>

            <div class="col-md-3 col-sm-3 col-xs-3 col-centered wow fadeInUp animated" data-wow-duration="250ms"
                 data-wow-delay="400ms">
                <div class="footer-single">
                    <h6>Get in Touch</h6>
                    <ul>
                        <li><a class="af"  href="https://twitter.com/hutomata"><i class="fa fa-twitter fa-lg"> </i> twitter</a></li>
                        <li><a class="af" href="https://www.facebook.com/hutoma.machine/"><i class="fa fa-facebook fa-lg"></i>
                                facebook</a></li>
                        <li><a class="af" href="https://www.linkedin.com/company/hutoma"><i class="fa fa-linkedin fa-lg"></i>
                                linkedin</a></li>
                    </ul>

                    </ul>
                </div>
            </div>

            <div class="col-md-3 col-sm-3 col-xs-3 col-centered wow fadeInUp animated" data-wow-duration="250ms"
                 data-wow-delay="500ms">
                <div class="footer-single">
                    <h6>Enterprise Customers</h6>
                    <ul>
                        <li><a class="af" href="mailto:hello@hutoma.com?subject=hutoma demo request">Schedule Demo</a></li>

                    </ul>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <p class="copyright text-center">
                        Copyright © 2016 hu:toma</a>
                    </p>
                </div>
            </div>
</footer>
</body>
</html>
