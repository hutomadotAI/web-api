<?php
require "../pages/config.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/entityApi.php";
require_once "../console/api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$entityApi = new \hutoma\api\entityApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
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
        echo json_encode($entities['entities']);
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
    <title>Hu:toma | Entities</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    
    <?php include_once "../console/common/google_tag_manager.php" ?>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showEntities('')">
    <?php include_once "../console/common/google_tag_manager_no_js.php" ?>

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

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/entity/entity.js"></script>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>

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
