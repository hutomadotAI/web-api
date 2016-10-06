<?php

    require "../pages/config.php";
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');

// fake request - we need to loading entity for a specific USER,AI, INTENT 
    $entityList = \hutoma\console::getIntegrations();
    $expressionList = \hutoma\console::getIntegrations();
    $parameterList = \hutoma\console::getIntegrations();


/*
    if ($entityList['status']['code'] !== 200) {
        unset($entityList);
        header('Location: ./error.php?err=3');
        exit;
    }
    */

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Edit Intent </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
    <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
    <link href="http://www.jqueryscript.net/css/jquerysctipttop.css" rel="stylesheet" type="text/css">
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini">
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
                    <?php include './dynamic/intent.element.content.head.html.php'; ?>
                    <?php include './dynamic/intent.element.content.expression.html.php'; ?>
                    <?php include './dynamic/intent.element.content.action.html.php'; ?>
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
<script src="./plugins/input-mask/jquery.inputmask.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./dist/js/demo.js"></script>
<script src="./plugins/intent/intent.element.js"></script>
<script src="./plugins/saveFile/FileSaver.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/jQuery/jquery.omniselect.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","intents",1,false,false]);
    </script>
</form>

<script>
    var entityListFromServer = <?php echo json_encode($entityList); unset($entityList);?>;
    var expressionListFromServer = <?php echo json_encode($expressionList); unset($expressionList);?>;
    var parameterListFromServer = <?php echo json_encode($parameterList); unset($parameterList);?>;
</script>
</body>
</html>
