<?php
require "../pages/config.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=119');
    exit;
}

if (isset($_POST['entity_name'])) {
    $entityName = $_POST['entity_name'];
    $retvalue = \hutoma\console::updateEntity($entityName, $_POST['entity_values']);

    if (isset($retvalue) && $retvalue['status']['code'] != 200) {
        \hutoma\console::redirect('./error.php?errObj=' . $retvalue['status']['info']);
        exit;
    }
} else {
    $entityName = $_POST['entity'];
}

$entity_values_list = \hutoma\console::getEntityValues($entityName);

if ($entity_values_list['status']['code'] !== 200) {
    unset($entity_values_list);
    \hutoma\console::redirect('./error.php?err=225');
    exit;
}

function isPostInputAvailable()
{
    return (isset($_POST['entity']) || isset($_POST['entity_name']));
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Edit Entity </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
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
                <div class="col-md-8">
                    <?php include './dynamic/entity.element.content.head.html.php'; ?>
                    <?php include './dynamic/entity.element.content.values.html.php'; ?>
                </div>
                <div class="col-md-4">
                    <?php include './dynamic/chat.html.php'; ?>
                    <?php include './dynamic/training.content.json.html.php'; ?>
                </div>
            </div>
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

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/saveFile/FileSaver.js"></script>
<script src="./plugins/validation/validation.js"></script>
<script src="./plugins/entity/entity.element.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "entities", 1, false, false]);
    </script>
</form>

<script>
    var entityValuesListFromServer = <?php echo json_encode($entity_values_list['entity_values']);  unset($entity_values_list);;?>;
</script>
</body>
</html>
