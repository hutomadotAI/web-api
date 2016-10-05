<?php
    require "../pages/config.php";
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');

    $response = \hutoma\console::getIntegrations(\hutoma\console::getDevToken());

    /* CHECK RESPONSE NEEDS API CALL
    if ($response['status']['code'] !== 200) {
        unset($response);
        \hutoma\console::redirect('./error.php?err=104');
        exit;
    }*/

?>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>hu:toma | integrations AI</title>
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

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showIntegrations('')">
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
    
          <div class="input-group-btn">
              <input class="form-control input-lg pull-right" onkeyup="searchIntegration(this.value)" value="" placeholder="Search">
          </div>
          <h2></h2>
          <p></p>
          <div class="tab-pane" id="tab_integration">
                <p id="intsearch"></p>
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
<script src="./plugins/integration/integration.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","integrations",2,true,false]);
    </script>
</form>


<script>
  // JSON RESPONSE NEEDS API CALL
  // var integrations = <?php  //echo json_encode($response['_integrationList']);  unset($response); ?>;
  var integrations = <?php  echo json_encode($response);  unset($response); ?>;

  var newNode = document.createElement('div');
  newNode.className = 'row';
  newNode.id = 'integrations_list';
</script>
<script>
  function searchIntegration(str) {
    showIntegrations(str);
  }
</script>
</body>
</html>
