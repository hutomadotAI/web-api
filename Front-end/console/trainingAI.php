<?php
    require "../pages/config.php";
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');


    if (isset($_POST['ai']) )
        CallGetSingleAI($_POST['ai']);

    function CallGetSingleAI($aiid){
        $singleAI = \hutoma\console::getSingleAI(\hutoma\console::getDevToken(),$aiid);
        if ($singleAI['status']['code'] === 200) {
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'] = $singleAI['aiid'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name'] = $singleAI['name'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description'] = $singleAI['description'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['created_on'] = $singleAI['created_on'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['private'] = $singleAI['is_private'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
            //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_debug_info'] = $singleAI['ai']['training_debug_info'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_status'] = $singleAI['training_status'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['status'] = $singleAI['ai_status'];
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['client_token'] = $singleAI['client_token'];

            //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_file']  = $singleAI['ai']['training_file\''];

            // NEED TO MODIFY CREATE AI IN 
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language'] = 'English';
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone']= 'GMT +00:00 UTC (UTC)';
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence']= 'Often';
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality']= 'No';
            $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['voice'] = 'Male';
            
        }else{
            unset($response);
            unset($singleAI);
            header("Location: ../error.php?err=15");
            exit;
        }
    }
?>

<!DOCTYPE html>

<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | training AI</title>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
    <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="./plugins/select2/select2.min.css">
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" id="trainingBody">
<div class="wrapper">
    <header class="main-header" id="headerID">
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
        <div class="row">
            <div class="col-md-7">
                <?php include './dynamic/training.content.upload.html.php'; ?>
                <?php include './dynamic/training.content.monitor.html.php'; ?>
                <?php include './dynamic/training.content.keys.html.php'; ?>
            </div>
            <div class="col-md-5">
                <?php include './dynamic/chat.html.php'; ?>
                <?php include './dynamic/training.content.json.html.php'; ?>
            </div>
        </div>
    </section>
    </div>

    <footer class="main-footer">
       <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

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
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<script>
    var lang = '<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language']; ?>';
    var voice = '<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['voice'] ?>';
</script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","training",1,true,false]);
    </script>
</form>
<script src="./plugins/training/training.area.js"></script>
</body>
</html>
