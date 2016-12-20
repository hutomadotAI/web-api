<?php
require "../pages/config.php";
require_once "../console/common/bot.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

// If is it set, it means the user has selected a existing AI from home list
if (isset($_POST['ai']))
    CallGetSingleAI($_POST['ai']);


function CallGetSingleAI($aiid)
{
    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);
    unset($aiApi);
    if ($singleAI['status']['code'] === 200) {
        setSessionVariables($singleAI);
    } else {
        unset($singleAI);
        \hutoma\console::redirect('../error.php?err=200');
        exit;
    }
    unset($singleAI);
}

function setSessionVariables($singleAI)
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'] = $singleAI['client_token'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $singleAI['name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $singleAI['description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = $singleAI['is_private'];
}

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Publish AI</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./plugins/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header" style="border:1px solid black;">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <?php include './dynamic/publishAI.content.html.php'; ?>
                </div>
            </div>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
    
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./bootstrap/js/bootstrap-filestyle.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/validation/validation.js"></script>
<script src="./plugins/select2/select2.full.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./plugins/dragdrop/dragdrop.js"></script>
<script src="./plugins/publish/publish.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "","home",0,true,true]);
    </script>
</form>

</body>
</html>