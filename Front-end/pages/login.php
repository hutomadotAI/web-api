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
    $login = \hutoma\console::login($identification, $password, isset($_POST['remember_me']));
    if($login === false){
      $msg = array("Error", $loginerror);
    }else if(is_array($login) && $login['status'] == "blocked"){
      $msg = array("Error", "Too many login attempts. You can attempt login after ". $login['minutes'] ." minutes (". $login['seconds'] ." seconds)");
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
    <link rel="stylesheet" href="../console/dist/css/hutoma.min.css">
    <link rel="stylesheet" href="../css/font-awesome.min.css">
    <link rel="stylesheet" href="../css/bootstrap.min.css">
    <link rel="stylesheet" href="../css/animate.css">
    <link rel="stylesheet" href="../css/main.css">
    <link rel="stylesheet" href="../console/plugins/cookiePolicyBar/cookiePolicyBar.css">
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="../js/modernizr-2.6.2.min.js"></script>
    <script type="text/javascript" src="../console/plugins/cookiePolicyBar/cookiePolicyBar.js"></script>
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
<body id="body">
<header id="navigation" class="navbar-fixed-top navbar">
    <div class="container" style="font-weight: bold">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <i class="fa fa-bars fa-2x"></i>
            </button>
            <a class="navbar-brand" href="#body">
                <h1 style="padding: 5px;" >
                   <b> hu:toma </b>
                </h1>
            </a>
        </div>
        <nav class="collapse navbar-collapse navbar-right" role="navigation">
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



<section>
<div
<div class="login-box">
    <div class="login-box-body" style="border: 1px solid #d2d6de;">
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

   <script>
      $(function () {
        $('input').iCheck({
          checkboxClass: 'icheckbox_square-blue',
          radioClass: 'iradio_square-blue',
          increaseArea: '20%' // optional
        });
      });
    </script>


  <script src="../js/custom.js"></script>

</body>
</html>
