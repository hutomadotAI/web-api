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

    <style>
        /* Sticky footer styles
        -------------------------------------------------- */
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
    <div class="register-box">
        <div class="register-box-body"  style="border: 1px solid #d2d6de; background-color: #202020;">
            <p class="login-box-msg"><b>password reset</b></p>
            <?php
            hutoma\console::forgotPassword();
            ?>
        </div>
    </div>
</section>

<?php include_once "./footer.php"; ?>

</body>
</html>
