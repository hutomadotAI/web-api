<?php
namespace hutoma;

require_once __DIR__ . "/../console/common/globals.php";
require_once __DIR__ . "/../console/common/utils.php";
require_once __DIR__ . "/../console/common/config.php";
require_once __DIR__ . "/../console/common/sessionObject.php";
require_once __DIR__ . "/../console/api/userMgmt.php";
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
    <title>Hu:toma | Reset Password</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="keywords" content="Deep learning, AI, Hutoma, Artificial Intelligence, Machine Learning, Siri, Cortana, Deep Learning API, AI Marketplace, Chatbots">
    <meta name="description" content="Hu:toma helps developers around the world build and monetize deep learning chatbots by providing free access to a proprietary platform offering both the tools and the channels to create and share conversational AIs.">
    <meta name="author" content="hutoma limited">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link rel="stylesheet" href="/console/dist/css/hutoma.css">
    <link rel="stylesheet" href="/console/dist/vendors/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/console/dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="/console/dist/vendors/cookiePolicyBar/cookiePolicyBar.css">

    <link rel="stylesheet" href="https://www.hutoma.ai/css/main.css">
    <link rel="icon" href="/console/dist/img/favicon.ico" type="image/x-icon">

    <script type="text/javascript" src="/console/dist/vendors/jQuery/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="/console/dist/vendors/cookiePolicyBar/cookiePolicyBar.js"></script>
    <script type="text/javascript" src="/console/dist/vendors/iCheck/icheck.min.js"></script>
    <script src='https://www.google.com/recaptcha/api.js'></script>

    <style>
        html {
            position: relative;
            min-height: 100%;
        }
    </style>
    <?php include_once "../console/common/google_tag_manager.php" ?>
</head>
<body class="web-body hold-transition register-page">
<?php include __DIR__ . "/../console/include/loggedout_header.php"; ?>
<section>
    <div class="register-box">
        <div class="register-box-body"  style="border: 1px solid #d2d6de; background-color: #202020;">
            <p class="login-box-msg"><b>password reset</b></p>

            <?php
            api\userMgmt::forgotPassword();
            ?>

        </div>
    </div>
</section>

<?php include __DIR__ . "/../console/include/loggedout_footer.php"; ?>

</body>
</html>
