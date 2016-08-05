<?php
    require "../pages/config.php";

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit();
    }

    if (!isset($_SESSION['current_ai_name'])){
        header('Location: ./error.php?err=6');
        exit();
    }

?>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | view all AIs</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
  <link rel="stylesheet" href="./dist/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/skins/skin-blue.min.css">
  <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
  <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css"> 

  <link rel="stylesheet" href="./plugins/bootstrap-slider/slider.css">
  <link rel="stylesheet" href="./dist/css/hutoma.css">
  <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<div class="wrapper">
    <header class="main-header">
    <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- =============================================== -->
    <!-- =============== CONTROLL PANEL ================ -->
    <!-- =============================================== -->
    <aside class="main-sidebar ">
    <section class="sidebar">
        <!-- ================ USER PANEL ================== -->
        <?php include './dynamic/userpanel.html.php'; ?>
        <!-- ================ USER ACTION ================= -->
        <ul class="sidebar-menu">
        <li class="header">WORKPLACE</li>
        <li class="active">
        <a href="#">
            <i class="fa fa-user"></i><span><?php echo($_SESSION['current_ai_name']); ?></span><i class="fa fa-ellipsis-v pull-right"></i>
        </a>
        <ul class="treeview-menu">
            <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap"></i> <span>training</span></a></li>
            <li><a href="./domainsAI.php"><i class="fa fa-th"></i>domains</a></li>
            <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt"></i>integration</a></li>
            <li><a href="./optionAI.php"><i class="fa fa  fa-gear"></i>AI options</a></li>
        </ul>
        </li>
        <li><a href="./newAi.php"><i class="fa fa-user-plus"></i>Create new AI</a></li>
        <li class="active"><a href="#"><i class="fa fa fa-list"></i>View all AI</a></li>
        <li><a href="./index.html"><i class="fa fa-commenting-o"></i> <span>intent</span></a></li>
        <li><a href="./index.html"><i class="fa fa-sitemap"></i> <span>entity</span></a></li>
      
        <li><a href="./index.html"><i class="fa fa-book"></i> <span>Documentation</span></a></li>
        <li class="header">ACTION</li>
        <li><a href="#"><i class="fa fa-arrow-circle-o-up text-green"></i> <span>Update</span></a></li>
        <li><a href="#"><i class="fa fa-user text-blue"></i> <span>Account</span></a></li>
        <li><a href="#"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
      </ul>
    </section>
    </aside>

    <!-- =============================================== -->
    <!-- ================ PAGE CONTENT ================= -->
    <!-- =============================================== -->
    <div class="content-wrapper">
    <section class="content">
    <?php include './dynamic/viewall.content.html.php'; ?>
    
    </section>
    </div>

    <footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <aside class="control-sidebar control-sidebar-dark">
    <?php include './dynamic/sidebar.controll.html.php'; ?>
    </aside>
</div>
    <script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
    <script src="./bootstrap/js/bootstrap.min.js"></script>
    <script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <script src="./plugins/fastclick/fastclick.min.js"></script>
    <script src="./dist/js/app.min.js"></script>
    <script src="./plugins/shared/shared.js"></script>
</body>
</html>