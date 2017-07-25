<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $aiApi->getIntegrations();
unset($aiApi);
if ($response['status']['code'] !== 200) {
    unset($response);
    \hutoma\console::redirect('./error.php?err=104');
    exit;
} else {
    $response = $response["integration_list"];
}

$aiName = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'];
$isExistAiId = isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Bot Integrations</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    <script src="scripts/external/autopilot/autopilot.js"></script>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showIntegrations('')">
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


            <?php include './dynamic/integrations.content.html.php'; ?>


        </section>
    </div>

    <!--
    <aside class="control-sidebar control-sidebar-dark">
    </aside>
    -->

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/integration/integration.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([
            "<?php echo $aiName;?>",
            "integrations",
            3,
            true,
            <?php echo $isExistAiId ? "false" : "true" ?>
        ]);
    </script>
</form>


<script>
    var integrations = <?php  echo json_encode($response);  unset($response); ?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'integrations_list';
</script>
<script>
    function searchIntegration(str) {
        showIntegrations(str);
    }
</script>
</body>
</html>
