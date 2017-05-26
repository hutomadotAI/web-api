<?php
require '../pages/config.php';
require_once "./api/apiBase.php";
require_once "./api/adminApi.php";
require_once "./api/aiApi.php";
require_once "./api/botApi.php";
require_once "./common/bot.php";
require_once "./common/utils.php";
require_once "./common/config.php";
require_once "./api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$api = new \hutoma\api\adminApi(\hutoma\console::isLoggedIn(), \hutoma\config::getAdminToken());
$userInfo = $api->getUserInfo($_SESSION['navigation_id']);
unset($api);

if(!isset($_SESSION[$_SESSION['navigation_id']]['user_details'])){
    $_SESSION[$_SESSION['navigation_id']]['user_details'] = $userInfo;
    $_SESSION[$_SESSION['navigation_id']]['user_details']['user_joined'] = \hutoma\console::joinedSince($_SESSION[$_SESSION['navigation_id']]['user_details']);
}

if(isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai'])){
    unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']);
    unset($_SESSION[$_SESSION['navigation_id']]['user_details']['bot']);
}

$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response_getAIs = $aiApi->getAIs();
unset($aiApi);

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
    <link rel="stylesheet" href="scripts/external/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <script type="text/javascript">(function(o){var b="https://api.autopilothq.com/anywhere/",t="cbf0f1bc32534e378a3344d5ded23c1b773ea5ca8b8548899be30488fc01d84b",a=window.AutopilotAnywhere={_runQueue:[],run:function(){this._runQueue.push(arguments);}},c=encodeURIComponent,s="SCRIPT",d=document,l=d.getElementsByTagName(s)[0],p="t="+c(d.title||"")+"&u="+c(d.location.href||"")+"&r="+c(d.referrer||""),j="text/javascript",z,y;if(!window.Autopilot) window.Autopilot=a;if(o.app) p="devmode=true&"+p;z=function(src,asy){var e=d.createElement(s);e.src=src;e.type=j;e.async=asy;l.parentNode.insertBefore(e,l);};if(!o.noaa){z(b+"aa/"+t+'?'+p,false)};y=function(){z(b+t+'?'+p,true);};if(window.attachEvent){window.attachEvent("onload",y);}else{window.addEventListener("load",y,false);}})({"app":true});</script>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
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
            <?php

            if (!isset($response_getAIs) || !(array_key_exists("ai_list", $response_getAIs))) {
                include './dynamic/home.content.first.html.php';
                include './dynamic/home.content.start.html.php';
            }
            else {
                include './dynamic/home.content.start.html.php';
                include './dynamic/home.viewall.html.php';
            }
            ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

</div>
<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/datatables/jquery.dataTables.js"></script>
<script src="scripts/external/datatables/dataTables.bootstrap.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/home/home.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>

<script>
    var aiList = <?php
        // HIDE AI INFOs NOT USED
        $tmp_list = [];
        if (isset($response_getAIs) && (array_key_exists("ai_list", $response_getAIs))) {
            $botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

            foreach ($response_getAIs['ai_list'] as $ai) {
                $publishingState = "NOT_PUBLISHED";
                $publishedBot = $botApi->getPublishedBot($ai['aiid']);

                if (isset($publishedBot) && $publishedBot['status']['code'] == 200) {
                    $publishingState = $publishedBot['bot']['publishingState'];
                }

                $v = array(
                    'aiid' => $ai['aiid'],
                    'name' => $ai['name'],
                    'description' => $ai['description'],
                    'ai_status' => $ai['ai_status'],
                    'publishing_state' => $publishingState
                );
                array_push($tmp_list, $v);
            }
            unset($botApi);
        }
        echo json_encode($tmp_list);
        unset($response_getAIs);
        unset($tmp_list);
        ?>;
</script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, false, true]);
    </script>
</form>

</body>
</html>