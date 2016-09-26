<?php
require "../pages/config.php";

    // fake request - here we need the intents from user on specified AI
    $integrations = \hutoma\console::getIntegrations();
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | intents </title>
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
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showIntents('')">
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
                <li><a href="./home.php"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>
                <li class="active">
                    <a href="#">
                        <i class="fa fa-user text-olive"></i><span><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                    </a>
                    <ul class="treeview-menu">
                        <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap text-purple"></i> <span>training</span></a></li>
                        <li class="active"><a href="#"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>
                        <li><a href="./entity.php"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>
                        <li><a href="./settingsAI.php"><i class="fa fa-gear text-black"></i>settings</a></li>
                    </ul>
                </li>
                <li>
                    <a href="#">
                        <i class="fa fa-book text-purple"></i> <span>Documentation</span><i class="fa fa-ellipsis-v pull-right"></i>
                    </a>
                    <ul class="treeview-menu">
                        <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>
                    </ul>
                </li>
            </ul>

            <ul class="sidebar-menu" style=" position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">MY ACCOUNT</li>
                <li><a href="./logout.php"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
    <section class="content">
            <div class="row">
                <div class="col-md-7">
                        <?php include './dynamic/intent.content.create.html.php'; ?>
                        <?php include './dynamic/intent.content.list.html.php'; ?>
                </div>
                <div class="col-md-5">
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
<script src="./plugins/intent/intent.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/shared/shared.js"></script>

<script>
    // FAKE API JSON REQUEST INTENTS RESPONSE
    var intents = <?php echo json_encode($integrations)?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'intents_list';



</script>
<script>
    function searchIntents(str) {
        showIntents(str);
    }
</script>
</body>
</html>
