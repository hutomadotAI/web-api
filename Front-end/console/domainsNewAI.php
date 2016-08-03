<?php
    require '../pages/config.php';

    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if (! isPostInputSet() ) {
        header("Location: ./error.php?err=2");
        exit;
    }

    fillSessionVariablesByPOST();


    $dev_token = \hutoma\console::getDevToken();
    $response = \hutoma\console::getDomains($dev_token);
    unset($dev_token);

    if ($response['status']['code'] !== 200) {
        unset($response);
        header('Location: ./error.php?err=3');
        exit;
    }

function isPostInputSet(){
    return  (
        isset($_POST['ai_name']) &&
        isset($_POST['ai_type']) &&
        isset($_POST['ai_language']) &&
        isset($_POST['ai_timezone']) &&
        isset($_POST['ai_confidence']) &&
        isset($_POST['ai_description'])
    );
}

function fillSessionVariablesByPOST(){

    $_SESSION['ai_name'] = $_POST['ai_name'];
    if ( $_POST['ai_type'] ==='public')
        $_SESSION['ai_private'] = 0;
    else
        $_SESSION['ai_private'] = 1;
    $_SESSION['ai_language'] = $_POST['ai_language'];
    $_SESSION['ai_timezone'] = $_POST['ai_timezone'];
    $_SESSION['ai_confidence'] = $_POST['ai_confidence'];
    $_SESSION['ai_description'] = $_POST['ai_description'];

}



?>

<!DOCTYPE html>
<html>
  <head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | domains new AI</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
  <link rel="stylesheet" href="./dist/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/hutoma.css">
  <link rel="stylesheet" href="./dist/css/skins/skin-blue.min.css">
  <link rel="stylesheet" href="./plugins/switch/switch.css">
  <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
  <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css"> 
  <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showDomains('',0)">
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
        <li class="header">WORKPLACE</li>
        <?php
            if (isset($_SESSION['current_ai_name'])) {
                echo('
                        <li>
                        <a href="#">
                          <i class="fa fa-user"></i><span>'.$_SESSION['current_ai_name'].'</span><i class="fa fa-ellipsis-v pull-right"></i>
                        </a>
                        <ul class="treeview-menu">
                            <li><a href="./trainingAI.php"><i class="fa fa-graduation-cap"></i> <span>training</span></a></li>
                            <li><a href="./domainsAI.php"><i class="fa fa-th"></i>domains</a></li>
                            <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt"></i>integration</a></li>
                            <li><a href="./optionAI.php"><i class="fa fa-gear"></i>AI options</a></li>
                        </ul>
                        </li>
                        <li class="active"><a href="#"><i class="fa fa-user-plus"></i>Create new AI</a></li>
                        <li><a href="./viewAllAI.php"><i class="fa fa fa-list"></i>View all AI</a></li>
                        <li><a href="./index.html"><i class="fa fa-commenting-o"></i> <span>intent</span></a></li>
                        <li><a href="./index.html"><i class="fa fa-sitemap"></i> <span>entity</span></a></li>
                  ');
              }
              else
                echo ('<li class="active"><a href="./newAI.php"><i class="fa fa-plus-circle"></i> <span>Create new AI</span></a></li>');
        ?>
        <li><a href="./documentation.php"><i class="fa fa-book"></i> <span>Documentation</span></a></li>
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
        
        <form method="POST" id="domainsNweAIform" action="./saveAI.php" onsubmit="domainsToJsonForPOST()">
            <a href="#" class="btn btn-primary flat" id="btnBack" onClick="history.go(-1); return false;">back</a>
            <button type="submit" class="btn btn-success flat" id="btnSave" value="" onClick="">save</button>
            <p></p>
            
            <div class="input-group-btn">
            <input class="form-control input-lg " value="" placeholder="Search" tabindex="0" onkeyup="searchDomain(this.value)">
            <input type="hidden" id="userActivedDomains"name="userActivedDomains" value="">
            </div>
            <p></p>
            
            <h2></h2>
            <p id="domsearch"></p>
    </form>
    <p></p>
    </section>
    </div>

    <footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <aside class="control-sidebar control-sidebar-dark">
    <?php include './dynamic/sidebar.controll.html.php'; ?>
    </aside>
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
  var domains = <?php  echo json_encode($response['domain_list']);  unset($response); ?>;

  // create a actived domains associative object - key is dom_id
  var userActived ={};
  for (var x in domains){
      var key = domains[x].dom_id;
      userActived[key]=false;
  }

  var newNode = document.createElement('div');
  newNode.className = 'row';
  newNode.id = 'domains_list';
</script>
<script>
  function searchDomain(str) {
    showDomains(str,0);
  }
</script>
</body>
</html>