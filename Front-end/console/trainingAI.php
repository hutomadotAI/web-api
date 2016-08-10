<?php
    require "../pages/config.php";


    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit;
    }

    if (isset($_POST['aiid']) ){
        generateSession();
    }


    if ( !isValuesSessionFilled() ){
        header('Location: ./error.php?err=2');
        exit;
    }

function generateSession(){

    $dev_token = \hutoma\console::getDevToken();
    $array = \hutoma\console::getSingleAI($dev_token,$_POST['aiid']);
    if ($array['status']['code']===200) {
        fillSessionVariables($array);
    }
    else {
        unset($array);
        exit;
    }
    unset($array);
}

function isValuesSessionFilled(){
    return
        isset($_SESSION['aiid']) &&
        isset($_SESSION['ai_name']) &&
        isset($_SESSION['ai_description']) &&
        isset($_SESSION['ai_created_on']) &&
        isset($_SESSION['ai_deep_learning_error']) &&
        isset($_SESSION["ai_training_debug_info"]) &&
        isset($_SESSION['ai_training_status']) &&
        isset($_SESSION['ai_language']) &&
        isset($_SESSION['ai_timezone']) &&
        isset($_SESSION['ai_confidence']) &&
        isset($_SESSION['ai_personality']) &&
        isset($_SESSION['ai_status']) &&

        //isset($_SESSION['ai_training_file']) &&                       // parameter missing
        isset($_SESSION['current_ai_name']) &&
        isset($_SESSION['userActivedDomains']);
}

function fillSessionVariables($array){
    $_SESSION['aiid'] = $array['ai']['aiid'];
    $_SESSION['ai_name'] = $array['ai']['name'];
    $_SESSION["ai_description"] = $array['ai']['description'];
    $_SESSION["ai_created_on"] = $array['ai']['created_on'];
    $_SESSION['ai_private'] = $array['ai']['is_private'];
    $_SESSION['ai_deep_learning_error'] = $array['ai']['deep_learning_error'];
    $_SESSION["ai_training_debug_info"] = $array['ai']["training_debug_info"];
    $_SESSION['ai_training_status'] =  $array['ai']['training_status'];

    $_SESSION['ai_language'] = 'COSTANT language';                      // parameter missing
    $_SESSION['ai_timezone'] = 'COSTANT GMT +00:00 UTC (UTC)';          // parameter missing
    $_SESSION["ai_confidence"] = '10';
    $_SESSION['ai_personality'] = 'default';                             // parameter missing
    $_SESSION['ai_status'] = $array['ai']['ai_status'];

    //$_SESSION['ai_training_file'] = $array['ai']['ai_trainingfile'];  // parameter missing
    $_SESSION['current_ai_name'] = $array['ai']['name'];
    $_SESSION['userActivedDomains'] = \hutoma\console::getDomains_and_UserActiveDomains($_SESSION['dev_id'], $_SESSION['aiid']);
}
?>

<!DOCTYPE html>

<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | training AI</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
  <link rel="stylesheet" href="./dist/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/hutoma.css">
  <link rel="stylesheet" href="./dist/css/skins/skin-blue.min.css">
  <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
  <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
  <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" id="trainingBody">
<div class="wrapper">
    <header class="main-header" id="headerID">
      <?php include './dynamic/header.html.php'; ?>
    </header>

    <aside class="main-sidebar ">
    <section class="sidebar">
        <!-- ================ USER PANEL ================== -->
        <?php include './dynamic/userpanel.html.php'; ?>

        <!-- ================ USER ACTION ================= -->
        <ul class="sidebar-menu">
            <li class="header">WORKPLACE</li>
            <li><a href="./home.php"><i class="fa fa-home"></i><span>home</span></a></li>
            <li class="active">
                <a href="#">
                    <i class="fa fa-user"></i><span><?php echo $_SESSION['current_ai_name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                </a>
                <ul class="treeview-menu">

                    <li class="active"><a href="#"><i class="fa fa-graduation-cap"></i> <span>training</span></a></li>
                    <li><a href="./intents.php"><i class="fa fa-commenting-o"></i> <span>intents</span></a></li>
                    <li><a href="./entities.html"><i class="fa fa-sitemap"></i> <span>entities</span></a></li>
                    <li><a href="./domainsAI.php"><i class="fa fa-th"></i>domains</a></li>
                    <li><a href="./integrationsAI.php"><i class="glyphicon glyphicon-list-alt"></i>integrations</a></li>
                    <li><a href="./settingsAI.php"><i class="fa fa-gear"></i>settings</a></li>
                </ul>
            </li>
            <li><a href="#"><i class="fa fa-book"></i> <span>Documentation</span></a></li>
            <li class="header">ACTION</li>
            <li><a href="#"><i class="fa fa-shopping-cart text-green"></i> <span>Marketplace</span></a></li>
            <li><a href="#"><i class="fa fa-user text-blue"></i> <span>Account</span></a></li>
            <li><a href="#"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
        </ul>
    </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
    <section class="content">
        <div class="row">
            <div class="col-md-7">
                <?php include './dynamic/training.content.upload.html.php'; ?>  
                <?php include './dynamic/training.content.monitor.html.php'; ?>
                <?php include './dynamic/training.content.domains.html.php'; ?>
                <?php include './dynamic/training.content.keys.html.php'; ?>
            </div>
            <div class="col-md-5">
                <?php include './dynamic/training.content.chat.html.php'; ?>
                <?php include './dynamic/training.content.json.html.php'; ?>
            </div>
        </div>

    </section>
    </div>



    <!-- =================== FOOTER =================== -->
    <footer class="main-footer">
       <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <!-- ================== SIDE BAR ================== -->
    <!--
    <aside class="control-sidebar control-sidebar-dark">
    </aside>
    -->
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./bootstrap/js/bootstrap-filestyle.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script src="./plugins/flot/jquery.flot.min.js"></script>
<script src="./plugins/flot/jquery.flot.resize.min.js"></script>
<script src="./plugins/flot/jquery.flot.pie.min.js"></script>
<script src="./plugins/flot/jquery.flot.categories.min.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/general/copyToClipboard.js"></script>
<script src="./plugins/shared/shared.js"></script>

</body>
</html>
