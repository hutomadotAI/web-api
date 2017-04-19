<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "api/botstoreApi.php";
require_once "common/bot.php";
require_once "common/developer.php";
require_once "common/botstoreItem.php";
require_once "common/botstoreListParam.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Botstore </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header">
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
            <div class="overlay carousel-ovelay" id ="carousel-overlay">
                <i class="fa fa-refresh fa-spin center-block"></i>
            </div>
            <p id="botsCarousels">
            </p>
            <p></p>
            <?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>
        </section>
    </div>
</div>

<footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
</footer>

<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "botstore", 2, false, false]);
    </script>
</form>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>

<script src="./scripts/botstore/botstoreWizard.js"></script>
<script src="./scripts/botstore/botstore.js"></script>
<script src="./scripts/botstore/carousel.js"></script>
<script src="./scripts/botcard/botcard.js"></script>
<script src="./scripts/botcard/buyBot.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<script>
    $(document).ready(function () {
        getCarousels(<?php if( isset($_GET['category']) ) echo json_encode($_GET['category']);?>);
    });
</script>
</body>
</html>