<?php
    require "../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }

    if (! isSessionVariablesAvailable() ) {
        \hutoma\console::redirect('./error.php?err=105');
        exit;
    }

    $AisMesh = \hutoma\console::getMesh($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);
    if ($AisMesh['status']['code'] == 200) $AisMesh = $AisMesh['mesh'];
    else $AisMesh ="";

    function isSessionVariablesAvailable(){
        return  (
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['description']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['language']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['timezone']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['confidence']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['personality']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['voice']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['private']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']) &&
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['client_token'])
        );
    }
?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | AI Settings </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./plugins/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet" href="./plugins/iCheck/all.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
    <link rel="stylesheet" href="./plugins/star/star.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showDomains('',1)">
<?php include_once "../console/common/google_analytics.php"; ?>

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

            <!-- Custom Tabs -->
            <div class="nav-tabs-custom flat no-shadow no-border">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#tab_general" data-toggle="tab">General</a></li>
                    <li><a href="#tab_aiskill" data-toggle="tab">AI Skills</a></li>
                </ul>

                <div class="tab-content" style="padding-bottom:0px;">
                    <!-- GENERAL TAB -->
                    <div class="tab-pane active" id="tab_general">
                        <?php include './dynamic/settings.content.general.html.php'; ?>
                    </div>
                    <!-- DOMAINS TAB -->
                    <div class="tab-pane" id="tab_aiskill">
                        <?php include './dynamic/settings.content.aiSkill.html.php'; ?>
                        <div class="row" style="background-color: #434343;">
                            <div class="col-lg-12" style="background-color: #434343; padding:5px;">
                                <?php include './dynamic/settings.content.aiSkill.list.html.php'; ?>
                            </div>
                            <?php include './dynamic/botstore.content.info.details.html.php'; ?>
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
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/select2/select2.min.js"></script>
<script src="./plugins/iCheck/icheck.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/clipboard/copyToClipboard.js"></script>
<script src="./plugins/clipboard/clipboard.min.js"></script>
<script src="./plugins/deleteAI/deleteAI.js"></script>
<script src="./plugins/domain/domain.js"></script>
<script src="./plugins/validation/validation.js"></script>
<script src="./plugins/inputCommon/inputCommon.js"></script>
<script src="./plugins/setting/setting.general.js"></script>
<script src="./plugins/setting/setting.aiSkill.js"></script>
<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","settings",1,true,false]);
    </script>
</form>

<script>
    var previousGeneralInfo = <?php echo json_encode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']); ?>;
    var domains = <?php  echo json_encode($AisMesh); unset($AisMesh)?>;

    var userActived ={};
    for (var x in domains) {
        var key = domains[x].aiid;
        userActived[key] = true;
    }

    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'domains_list';

    function searchDomain(str) { showDomains(str,1);}
</script>
</body>
</html>
