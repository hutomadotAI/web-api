<?php
require "../pages/config.php";
require_once "./api/apiBase.php";

if(!\hutoma\console::checkSessionIsActive()){
     exit;
}

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['botid'])) {
    \hutoma\console::redirect('./error.php?err=100');
    exit;
}

$name = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'];
$menu_title = $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['menu_title'];

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Botstore - box</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" style="background:#2c3b41;">
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
            <div class="row">
                <div class="col-md-12">
                    <iframe src="botcardDetail.php" frameBorder="0" scrolling="no" class="iframe-full-height">Browser not compatible.</iframe>
                </div>
            </div>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/botstore/botstoreWizard.js"></script>
<script src="./scripts/botcard/buyBotFromBotcardDetail.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>

<script>
    $('.iframe-full-height').on('load', function(){
        this.style.height=this.contentDocument.body.scrollHeight + 'px';
        this.style.width= '100%';
    });
</script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        var info = infoSidebarMenu("<?php echo $menu_title;?>");
        MENU.init(["<?php echo $name; unset($name); ?>", info['menu_label'], info['menu_level'], info['menu_block'], <?php echo json_encode(!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])); ?>]);
    </script>
</form>
</body>
</html>
