<?php
require "../pages/config.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/integrationApi.php";
require_once "../console/api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$integrationApi = new \hutoma\api\integrationApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
    \hutoma\console::redirect('./error.php?err=200');
    exit;
}
$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];

$connect_token = $_GET["code"];
if (isset($connect_token)) {
    $redir = $_COOKIE["facebookRedir"];
    $connect_result = $integrationApi->setConnectToken($aiid, $connect_token, $redir);
    $_SESSION[$_SESSION['navigation_id']]['fb_connect_result'] = $connect_result;
    \hutoma\console::redirect($redir);
    exit();
}
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | integrations </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <script src="scripts/external/autopilot/autopilot.js"></script>
    <script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
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
    <div class="content-wrapper" style="margin-right:350px;">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow">
                    <?php include './dynamic/integrations.facebook.html.php'; ?>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- ================ CHAT CONTENT ================= -->
    <aside class="control-sidebar control-sidebar-dark control-sidebar-open">
        <?php include './dynamic/chat.html.php'; ?>
        <?php include './dynamic/training.content.json.html.php'; ?>
    </aside>

    <footer class="main-footer" style="margin-right:350px;">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/validation/validation.js"></script>
<xscript src="./scripts/entity/entity.js"></xscript>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "integrations", 1, true, false]);
    </script>
</form>

</body>
</html>
