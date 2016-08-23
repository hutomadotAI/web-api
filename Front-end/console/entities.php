<?php
require "../pages/config.php";
$integrations = \hutoma\console::getIntegrations();
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
    <link rel="stylesheet" href="./dist/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
    <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showEntities('')">
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
                <li class="header">WORKPLACE</li>
                <li><a href="./home.php"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>
                <li class="active">
                    <a href="#">
                        <i class="fa fa-user text-olive"></i><span><?php echo $_SESSION['current_ai_name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                    </a>
                    <ul class="treeview-menu">

                        <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap text-purple"></i> <span>training</span></a></li>
                        <li><a href="./intents.php"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>
                        <li class="active"><a href="./entities.php"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>
                        <li><a href="./domainsAI.php"><i class="fa fa-th text-red"></i> <span>domains</span></a></li>
                        <li><a href="#"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>
                        <li><a href="./settingsAI.php"><i class="fa fa-gear text-black"></i>settings</a></li>
                    </ul>
                </li>
                <li><a href="#"><i class="fa fa-book text-purple"></i> <span>Documentation</span></a></li>
            </ul>

            <ul class="sidebar-menu" style=" position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">ACTION</li>
                <li><a href="#"><i class="fa fa-shopping-cart text-green" style="position: relative;"></i> <span>Marketplace</span></a></li>
                <li><a href="#"><i class="fa fa-user text-blue"></i> <span>Account</span></a></li>
                <li><a href="./logout.php"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">

            <div class="input-group-btn">
                <input class="form-control input-lg pull-right" onkeyup="searchEntities(this.value)" value="" placeholder="Search">
            </div>
            <h2></h2>
            <p></p>
            <div class="tab-pane" id="tab_entities">
                <p id="entsearch"></p>
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
<script src="./plugins/entities/entities.js"></script>
<script src="./plugins/shared/shared.js"></script>

<script>
    // FAKE API JSON REQUEST INTEGRATION RESPONSE

    var entities = <?php echo json_encode($integrations)?>;
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
