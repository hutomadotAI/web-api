<?php
    require '../pages/config.php';

    $_SESSION[ $_SESSION['navigation_id'] ]['user_details'] = \hutoma\console::getUser();
    $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['user_joined'] = \hutoma\console::joinedSince($_SESSION[ $_SESSION['navigation_id'] ]['user_details']);

    $response_getAIs = \hutoma\console::getAIs(\hutoma\console::getDevToken());
?>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | API home</title>
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
           <li class="active"><a href="#"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>
           <li><a href="#"><i class="fa fa-book text-purple"></i> <span>Documentation</span></a></li>
        </ul>

        <ul class="sidebar-menu" style=" position: absolute; bottom:0; width: 230px; min-height: 135px;">
            <li class="header" style="text-align: center;">MY ACCOUNT</li>
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
    <?php
        include './dynamic/home.content.start.html.php';
        include './dynamic/home.viewall.html.php';
    ?>
    </section>
    </div>
    
    <footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

</div>
<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/home/home.js"></script>
<script src="./plugins/shared/shared.js"></script>

</body>
</html>