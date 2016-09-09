<?php
    require "../pages/config.php";

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if (!isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'])){
        header('Location: ./error.php?err=2');
        exit;
    }
?>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | option AI</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
  <link rel="stylesheet" href="./dist/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
  <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
  <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css"> 

  <link rel="stylesheet" href="./plugins/select2/select2.min.css">
  <link rel="stylesheet" href="./plugins/bootstrap-slider/slider.css">
  <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.css">
  <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.skinHTML5.css">
  <link rel="stylesheet" href="./plugins/switch/switch.css">
  <link rel="stylesheet" href="./dist/css/hutoma.css">
  <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">

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
            <li class="header">WORKPLACE</li>
            <li><a href="./home.php"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>
            <li class="active">
                <a href="#">
                    <i class="fa fa-user text-olive"></i><span><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                </a>
                <ul class="treeview-menu">

                    <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap text-purple"></i> <span>training</span></a></li>
                    <li><a href="./intent.php"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>
                    <li><a href="./entity.php"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>
                    <li><a href="./domainsAI.php"><i class="fa fa-th text-red"></i> <span>domains</span></a></li>
                    <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>
                    <li class="active"><a href="#"><i class="fa fa-gear text-black"></i>settings</a></li>
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

    <!-- =============================================== -->
    <!-- ================ PAGE CONTENT ================= -->
    <!-- =============================================== -->
    <div class="content-wrapper">
    <section class="content">

    <!-- Custom Tabs -->
    <div class="nav-tabs-custom flat no-shadow">
        <ul class="nav nav-tabs">
        <li class="active"><a href="#tab_general" data-toggle="tab">General</a></li>
        <li><a href="#tab_emotion" data-toggle="tab">Emotions</a></li>
        </ul>
                
        <div class="tab-content">
              <!-- GENERAL TAB -->
              <div class="tab-pane active" id="tab_general">
                  <?php include './dynamic/settings.content.input.html.php'; ?>
              </div>
              <!-- EMOTION TAB -->
              <div class="tab-pane" id="tab_emotion">
                  <?php include './dynamic/settings.content.emotion.html.php'; ?>
              </div>
        </div>
    </div>
    </section>
    </div>

    <footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <!--
    <aside class="control-sidebar control-sidebar-dark">
    </aside>
    -->

</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/select2/select2.full.min.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
<script src="./plugins/clipboard/copyToClipboard.js"></script>
<script src="./plugins/deleteAI/deleteAI.js" type="text/javascript"></script>
<script src="./plugins/shared/shared.js"></script>

<script> $(function () { $(".select2").select2(); }); </script>

<script>
    $(function () {
        $('.slider').slider();
        $("#confidence").ionRangeSlider({
            type: "single",
            min: 1,
            max: 10,
            from:5,
            from_value:"normal",
            step: 1,
            grid: true,
            keyboard: true,
            onStart: function (data) {console.log("onStart"); },
            onChange: function (data) {console.log("onChange"); },
            onFinish: function (data) { console.log("onFinish"); },
            onUpdate: function (data) {console.log("onUpdate"); },
            values: ["very bad", "bad", "little bad","mediocre", "normal", "good","very good", "excellent", "genius", "oracle"]
        });
    });
</script>
</body>
</html>
