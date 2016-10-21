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
    <title>hutoma | password reset</title>
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
            /* Margin bottom by footer height */
            margin-bottom: 500px;

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
<body>
<?php include_once "../console/common/google_analytics.php"; ?>

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


    <div class="register-box-body"  style="border: 1px solid #d2d6de;">
        <p class="login-box-msg"><b>password reset</b></p>

        <?php

        hutoma\console::forgotPassword();
        ?>

    </div>
</div>

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
