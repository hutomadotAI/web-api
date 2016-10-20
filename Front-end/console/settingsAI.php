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

    $response = \hutoma\console::getDomains(\hutoma\console::getDevToken());
    if ($response['status']['code'] !== 200) {
        unset($response);
        header('Location: ./error.php?err=3');
        exit;
    }


    /*
    $usr_domains = \hutoma\console::getDomains_and_UserActiveDomains(\hutoma\console::getDevToken(),$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);
    if ($usr_domains['status']['code'] !== 200) {
        unset($usr_domains);
        header('Location: ./error.php?err=3');
        exit;
    }
    */

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
    <title>hu:toma | option AI</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./plugins/select2/select2.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">

    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.skinHTML5.css">
    <link rel="stylesheet" href="./plugins/iCheck/all.css">

    <link rel="stylesheet" href="./plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.skinHTML5.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showDomains('',1)">
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
            <div class="nav-tabs-custom flat no-shadow">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#tab_general" data-toggle="tab">General</a></li>
                    <li><a href="#tab_domains" data-toggle="tab">Pre-training Neural Networks</a></li>
                </ul>

                <div class="tab-content">
                    <!-- GENERAL TAB -->
                    <div class="tab-pane active" id="tab_general">
                        <?php include './dynamic/settings.content.input.html.php'; ?>
                    </div>
                    <!-- DOMAINS TAB -->
                    <div class="tab-pane" id="tab_domains">
                        <?php include './dynamic/settings.content.domains.html.php'; ?>
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
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/select2/select2.full.min.js"></script>
<script src="./plugins/iCheck/icheck.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/clipboard/copyToClipboard.js"></script>
<script src="./plugins/clipboard/clipboard.min.js"></script>
<script src="./plugins/deleteAI/deleteAI.js"></script>
<script src="./plugins/domain/domain.js"></script>
<script src="./plugins/inputCommon/inputCommon.js"></script>
<script src="./plugins/setting/setting.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","settings",1,true,false]);
    </script>
</form>

<script>
    var previousField = <?php echo json_encode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']); ?>;
</script>

<script>
    // API JSON REQUEST DOMAIN RESPONSE

    var domains = <?php  echo json_encode($response['_domainList']);  unset($response); ?>;
    var userActived ={};
    for (var x in domains){
        var key = domains[x].dom_id;
        userActived[key]=false;
    }

    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'domains_list';

    /* NEED BIND WITH API DATA STORED
    var domains = <?php //echo json_encode($response['_domainList']); unset($response);?>;
    var usr_domains =<?php //echo json_encode($usr_domains); unset($usr_domains);?>;

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
    */
</script>

<script>
    function searchDomain(str) { showDomains(str,1);}
</script>


</body>
</html>
