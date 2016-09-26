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
    <title>hutoma | registration page</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="keywords"
          content="Deep learning, AI, Hutoma, Artificial Intelligence, Machine Learning, Siri, Cortana, Deep Learning API">
    <meta name="description"
          content="Hutoma builds emotionally evolved AIs and Digital Employees that can have intelligent conversations with you or your customers.">
    <meta name="author" content="hutoma limited">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="../css/font-awesome.min.css">
    <link rel="stylesheet" href="../css/bootstrap.min.css">
    <link rel="stylesheet" href="../css/animate.css">
     <link rel="stylesheet" href="../console/dist/css/hutoma.min.css">
    <link rel="stylesheet" href="../css/main.css">
    <script src="../js/modernizr-2.6.2.min.js"></script>
    <script type="text/javascript" src="../js/jquery-1.10.1.min.js"></script>


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
  font-family: 'Muli', 'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
body {
  /* Margin bottom by footer height */
  margin-bottom: 500px;
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


/* Custom page CSS
-------------------------------------------------- */
/* Not required for template or sticky footer method. */

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
      if(isset($_POST['submit'])){

     #  When commenting out this code make sure you use the secured curlHelper instead of file_get_contents
     #  to obtain the response from the captcha service
     #   if(isset($_POST['g-recaptcha-response'])) {
     #     $captcha=$_POST['g-recaptcha-response'];
     #     $response=json_decode(file_get_contents("https://www.google.com/recaptcha/api/siteverify?secret=6LcArBsTAAAAAMWrUUlxsiK9Cg9fJiIYroRycv_z&response=".$captcha."&remoteip=".$_SERVER['REMOTE_ADDR']), true);
     #   if($response['success'] == false)
     #   {
     #      $captcha  ='<div class="alert alert-warning">';
     #      $captcha .='<i class="icon fa fa-exclamation"></i> You did not passed the captcha test';
     #      $captcha .='</div>';
     #      return $captcha;
     #   }
     # }
     # else
     #  {
     #   $captchamsg  ='<div class="alert alert-warning">';
     #   $captchamsg .='<i class="icon fa fa-exclamation"></i> Please check the captcha checkbox';
     #   $captchamsg .='</div>';
     #   return $captchamsg;
     #   }

        $email = $_POST['email'];
        $password = $_POST['pass'];
        $retyped_password = $_POST['retyped_password'];
        $name = $_POST['username'];
        $terms = isset($_POST['terms']);

        $missingfields  ='<div class="alert alert-warning">';
        $missingfields .='<i class="icon fa fa-exclamation"></i> Some Fields were left blank.';
        $missingfields .='</div>';

        $passwordmismatch  ='<div class="alert alert-warning">';
        $passwordmismatch .='<i class="icon fa fa-exclamation"></i> The Passwords you entered do not match.';
        $passwordmismatch .='</div>';

        $termsmsg  ='<div class="alert alert-warning">';
        $termsmsg .='<i class="icon fa fa-exclamation"></i> Please indicate that you have read and agree to the Terms and Conditions and Privacy Policy';
        $termsmsg .='</div>';

        $userexists  ='<div class="alert alert-warning">';
        $userexists .='<i class="icon fa fa-exclamation"></i> This user already exists.';
        $userexists .='</div>';

        $msg= $missingfields;

        if( $email == "" || $password == '' || $retyped_password == '' || $name == '' ) $msg= $missingfields;
          elseif($password != $retyped_password) $msg= $passwordmismatch;
            elseif($terms != 'True') $msg= $termsmsg;
        else{
          $createAccount = \hutoma\console::register($email, $password, $email, $name, date("Y-m-d H:i:s"));

          if($createAccount === "exists"){
             $msg= $userexists;
          }elseif($createAccount === true){
            // redirect to the login page
            \hutoma\console::logout();
            exit();
          }
          else  $msg= $createAccount;

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
                  <h1 style="padding: 5px;font-family: 'Muli', 'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif;" >
                     <b> hu:toma </b>
                  </h1>
              </a>
          </div>
          <nav class="collapse navbar-collapse navbar-right" role="navigation">
              <ul id="nav" class="nav navbar-nav">
                  <li class="current"><a href="../index.html" class="external">Home</a></li>
                  <li><a href="./login.php" class="external">Login</a></li>
                  <li><a href="../about.html">Features</a></li>
                  <li><a href="../about.html" class="external">About</a></li>
                  <li><a href="../contactus.html" class="external">Contacts</a></li>
              </ul>
          </nav>
      </div>
  </header>

<div class="register-box">


      <div class="register-box-body">
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
          <div class="row">
            <div class="col-xs-8">
              <div class="checkbox icheck" style="margin-left:20px">
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
          <br/>

        <!--   <div class="g-recaptcha" data-sitekey="6LcArBsTAAAAADPS78hYLKb05FNfwY0cMQBJZLAV"></div> -->

        </form>

     <!--    <div class="social-auth-links text-center">
          <p>- OR -</p>
          <a href="#" class="btn btn-block btn-social btn-facebook btn-flat"><i class="fa fa-facebook"></i> Sign up using Facebook</a>
          <a href="#" class="btn btn-block btn-social btn-google btn-flat"><i class="fa fa-google-plus"></i> Sign up using Google+</a>
        </div>
 -->
        <a href="login.php" class="text-center newa">I already have an account</a>
      </div><!-- /.form-box -->
    </div><!-- /.register-box -->

    <!-- jQuery 2.1.4 -->
    <script src="../console/plugins/jQuery/jQuery-2.1.4.min.js"></script>
    <!-- Bootstrap 3.3.5 -->
    <script src="../console/bootstrap/js/bootstrap.min.js"></script>


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
  <script src="../js/wow.min.js"></script>

  <script>
    var wow = new WOW ({
        boxClass:     'wow',      // animated element css class (default is wow)
        animateClass: 'animated', // animation css class (default is animated)
        offset:       120,          // distance to the element when triggering the animation (default is 0)
        mobile:       false,       // trigger animations on mobile devices (default is true)
        live:         true        // act on asynchronously loaded content (default is true)
        }
      );
      wow.init();
  </script>



  <script src="../js/custom.js"></script>

</body>
</html>
