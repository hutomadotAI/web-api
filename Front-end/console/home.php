<?php
    require '../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        \hutoma\console::redirect('./error.php?err=100');
        exit;
    }

    /*
    if ( !isset($_SESSION['navigation_id'])) {
        \hutoma\console::redirect('./error.php?err=102');
        exit;
    }
    */

    $_SESSION[ $_SESSION['navigation_id'] ]['user_details'] = \hutoma\console::getUser();
    //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['user_joined'] = \hutoma\console::joinedSince($_SESSION[ $_SESSION['navigation_id'] ]['user_details']);
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

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
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
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "","home",0,true,true]);
    </script>
</form>


</body>
</html>