<?php
require "../pages/config.php";

\hutoma\telemetry::getInstance()->log(\hutoma\TelemetryEvent::ERROR, "errorpage", $_GET['err'],
    array(
        "referrer" => $_SERVER['HTTP_REFERER'],
        "errorObject" => isset($_GET['errObj']) ? $_GET['errObj'] : ""
    ));

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     exit;
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma |ERROR PAGE</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="scripts/external/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <aside class="main-sidebar ">
        <section class="sidebar">
            <!-- ================ USER ACTION ================= -->
            <ul class="sidebar-menu disabled" style="position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">MY ACCOUNT</li>
                <li><a href="./logout.php"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <?php include './dynamic/error.content.html.php'; ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
</body>
</html>