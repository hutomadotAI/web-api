<?php

    require "../pages/config.php";
    if (!isset($_POST['entity']) ) {
        header('Location: ./error.php?err=17');
        exit();
    }
    // fake request - we need to loading entity keys for a specific USER,AI
    $entityKeys = \hutoma\console::getIntegrations();



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
    <title>hu:toma | Edit Entity </title>
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
    <link rel="stylesheet" href="./plugins/iCheck/skins/square/red.css">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">


    <link href="http://www.jqueryscript.net/css/jquerysctipttop.css" rel="stylesheet" type="text/css">

</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini">
<div class="wrapper">
    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <aside class="main-sidebar ">
        <section class="sidebar">
            <!-- ================ USER PANEL ================== -->
            <?php include './dynamic/userpanel.html.php'; ?>

            <!-- ================ USER ACTION ================= -->
            <ul class="sidebar-menu">
                <li class="header" style="text-align: center;">CONSOLE</li>
                <li><a href="./home.php" tabindex="-1"><i class="fa fa-home text-light-blue" tabindex="-1"></i><span>home</span></a></li>
                <li class="active">
                    <a href="#" tabindex="-1">
                        <i class="fa fa-user text-olive" tabindex="-1" ></i><span><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                    </a>
                    <ul class="treeview-menu">

                        <li><a href="./trainingAI.php" tabindex="-1"><i class="fa fa-graduation-cap text-purple" tabindex="-1"></i> <span>training</span></a></li>
                        <li><a href="./intent.php" tabindex="-1"><i class="fa fa-commenting-o text-green" tabindex="-1"></i><span>intents</span></a></li>
                        <li class="active"><a href="./entity.php"tabindex="-1" ><i class="fa fa-sitemap text-yellow" tabindex="-1"></i> <span>entities</span></a></li>
                        <li><a href="./domainsAI.php" tabindex="-1"><i class="fa fa-th text-red" tabindex="-1"></i> <span>domains</span></a></li>
                        <li><a href="./integrations.php" tabindex="-1" ><i class="glyphicon glyphicon-list-alt text-default" tabindex="-1"></i>integrations</a></li>
                        <li><a href="./settingsAI.php" tabindex="-1"><i class="fa fa-gear text-black" tabindex="-1"></i>settings</a></li>
                    </ul>
                </li>
                <li><a href="#" tabindex="-1"><i class="fa fa-book text-purple" tabindex="-1"></i> <span>Documentation</span></a></li>
            </ul>

            <ul class="sidebar-menu" style=" position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">MY ACCOUNT</li>
                <li><a href="#" tabindex="-1" ><i class="fa fa-shopping-cart text-green" style="position: relative;" tabindex="-1" ></i> <span>Marketplace</span></a></li>
                <li><a href="#" tabindex="-1" ><i class="fa fa-user text-blue" tabindex="-1" ></i> <span>Account</span></a></li>
                <li><a href="./logout.php" tabindex="-1" ><i class="fa fa-power-off text-red" tabindex="-1" ></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-8">
                    <?php include './dynamic/entity.element.content.head.html.php'; ?>
                    <?php include './dynamic/entity.element.content.keys.html.php'; ?>
                    <?php include './dynamic/entity.element.content.prompt.html.php'; ?>
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
<script src="./plugins/iCheck/icheck.js"></script>
<script src="./plugins/entity/entity.element.js"></script>
<script src="./plugins/saveFile/FileSaver.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/jQuery/jquery.omniselect.js"></script>
<script>
    var entityKeysListFromServer = <?php echo json_encode($entityKeys); unset($entityKeys);?>;
</script>
</body>
</html>
