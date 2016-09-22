<?php
    require "../pages/config.php";
    
    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit();
    }

    if (!isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'])){
        header('Location: ./error.php?err=10');
        exit();
    }

    $response = \hutoma\console::getDomains(\hutoma\console::getDevToken());

    if ($response['status']['code'] !== 200) {
        unset($response);
        header('Location: ./error.php?err=3');
        exit;
    }
    $usr_domains = \hutoma\console::getDomains_and_UserActiveDomains(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);
?>

<!DOCTYPE html>
<html>
  <head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | domains AI</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
  <link rel="stylesheet" href="./dist/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/hutoma.css">
  <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
  <link rel="stylesheet" href="./plugins/switch/switch.css">
  <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
  <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css"> 
  <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showDomains('',1)" id="body">
<div class="wrapper">

    <header class="main-header">
    <?php include './dynamic/header.html.php'; ?>
    </header>

    <aside class="main-sidebar ">
    <section class="sidebar">
        <!-- ================ USER PANEL ================== -->
        <?php include './dynamic/userpanel.html.php'; ?>
        <!-- ================ USER ACTION ================== -->

        <ul class="sidebar-menu">
            <li class="header" style="text-align: center;">CONSOLE</li>
            <li><a href="./home.php"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>
            <li class="active">
                <a href="#">
                    <i class="fa fa-user text-olive"></i><span><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                </a>
                <ul class="treeview-menu">

                    <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap text-purple"></i> <span>training</span></a></li>
                    <li><a href="./intent.php"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>
                    <li><a href="./entity.php"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>
                    <li class="active"><a href="#"><i class="fa fa-th text-red"></i>domains</a></li>
                    <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>
                    <li><a href="./settingsAI.php"><i class="fa fa-gear text-black"></i>settings</a></li>
                </ul>
            </li>
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
        <?php include './dynamic/domainsAI.content.html.php'; ?>
    </section>
    </div>

    <footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <!--
    <aside class="control-sidebar control-sidebar-dark">
    </aside>
    -->
</div><!-- ./wrapper -->

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
<script src="./plugins/daterangepicker/daterangepicker.js"></script>
<script src="./plugins/domain/domain.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script>
  // API JSON REQUEST DOMAIN RESPONSE
  var domains = <?php echo json_encode($response['_domainList']); unset($response);?>;
  var usr_domains =<?php echo json_encode($usr_domains); unset($usr_domains);?>;

  var userActived ={};
  for (var x in domains){
      var key = usr_domains[x].dom_id;
      if(key!=null)
          userActived[key] = usr_domains[x].active;
      else
          userActived[domains[x].dom_id] = false;
  }
  var newNode = document.createElement('div');
  newNode.className = 'row';
  newNode.id = 'domains_list';
</script>
<script>
  function searchDomain(str) {
    showDomains(str,1);
  }
</script>
</body>
</html>