<?php
require "../pages/config.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/entityApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

$entityApi = new \hutoma\api\entityApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

if (isset($_REQUEST['deleteentity'])) {
    $entityName = $_REQUEST['deleteentity'];
    $result = $entityApi->deleteEntity($entityName);
    if ($result['status']['code'] != 200) {
        unset($result);
        \hutoma\console::redirect('./error.php?err=326');
    }
}

$entities = $entityApi->getEntities();
unset($entityApi);

if ($entities['status']['code'] !== 200 && $entities['status']['code'] !== 404) {
    unset($entities);
    \hutoma\console::redirect('./error.php?err=225');
    exit;
}

function echoJsonEntitiesResponse($entities)
{
    if ($entities['status']['code'] !== 404) {
        echo json_encode($entities['entity_name']);
    }
    else
        echo '""'; // return empty string
}
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | entities </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showEntities('')">
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
                    <?php include './dynamic/entity.content.create.html.php'; ?>
                    <?php include './dynamic/entity.content.list.html.php'; ?>
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

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/validation/validation.js"></script>
<script src="./plugins/entity/entity.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "entities", 1, true, false]);
    </script>
</form>

<script>
    var entities = <?php echoJsonEntitiesResponse($entities); unset($entities)?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'entities_list';
</script>
<script>
    function searchEntities(str) {
        showEntities(str);
    }
</script>
</body>
</html>