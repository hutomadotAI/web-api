<?php
    require '../pages/config.php';
    require_once "../console/api/apiBase.php";
    require_once "../console/api/aiApi.php";

    if(!\hutoma\console::checkSessionIsActive()){
        exit;
    }

    function isPreviousFieldsFilled(){
        return  (
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['voice'])
        );
    }


    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $response_getAIs = $aiApi->getAIs();
    unset($aiApi);

    $name_list='';
    if (isset($response_getAIs) && (array_key_exists("ai_list",$response_getAIs))) {
        for ($i = 0, $l = count($response_getAIs['ai_list']); $i < $l; ++$i)
            $name_list[$i] = $response_getAIs['ai_list'][$i]['name'];
    }

    unset($response_getAIs);
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Create a New AI</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

    <link rel="stylesheet" href="scripts/external/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="scripts/external/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet" href="scripts/external/iCheck/all.css">
    
    
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header" style="border:1px solid black;">
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
            <?php include './dynamic/newAI.content.html.php'; ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
    
</div>

<script>
    var name_list = <?php echo json_encode($name_list); unset($name_list);?>;
    var previousFilled = <?php if (isPreviousFieldsFilled()) echo('true'); else echo ('false'); ?>;
    var previousGeneralInfo  = <?php if (isPreviousFieldsFilled()) echo json_encode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']); else echo 'false';?>;
    var err = <?php if(isset($_GET['err'])) echo($_GET['err']); else echo ('false'); ?>;
</script>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/inputCommon/inputCommon.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/createAI/createAI.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>
<script src="scripts/external/bootstrap-slider/bootstrap-slider.js"></script>
<script src="scripts/external/ionslider/ion.rangeSlider.min.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, false, true]);
    </script>
</form>
</body>
</html>