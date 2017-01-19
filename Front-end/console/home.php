<?php
require '../pages/config.php';
require_once "./api/apiBase.php";
require_once "./api/aiApi.php";
require_once "./api/botApi.php";
require_once "./common/bot.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

$_SESSION[$_SESSION['navigation_id']]['user_details'] = \hutoma\console::getUser();
$_SESSION[$_SESSION['navigation_id']]['user_details']['user_joined'] = \hutoma\console::joinedSince($_SESSION[$_SESSION['navigation_id']]['user_details']);

if(isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']))
    unset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']);

$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response_getAIs = $aiApi->getAIs();
unset($aiApi);

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$publishedBots = $botApi->getPublishedBots();
unset($botApi);

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
    <link rel="stylesheet" href="./plugins/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
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
            include './dynamic/home.content.start.html.php';
            if (!isset($response_getAIs) || !(array_key_exists("ai_list", $response_getAIs)))
                include './dynamic/home.content.first.html.php';
            else
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
<script src="./plugins/datatables/jquery.dataTables.js"></script>
<script src="./plugins/datatables/dataTables.bootstrap.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/home/home.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<script>
    var aiList = <?php
        // HIDE AI INFOs NOT USED
        $tmp_list = [];
        if (isset($response_getAIs) && (array_key_exists("ai_list", $response_getAIs))) {
            foreach ($response_getAIs['ai_list'] as $ai) {
                $v = array(
                    'aiid' => $ai['aiid'],
                    'name' => $ai['name'],
                    'description' => $ai['description'],
                    'ai_status' => $ai['ai_status']
                );
                array_push($tmp_list, $v);
            }
        }
        echo json_encode($tmp_list);
        unset($response_getAIs);
        unset($tmp_list);
        ?>;

    var publishedBots = <?php
        $tmp_published_list = [];
        if (isset($publishedBots) && (array_key_exists("bots", $publishedBots))) {
            foreach ($publishedBots['bots'] as $botDetails) {
                $publishedBot = new \hutoma\bot();
                array_push($tmp_published_list, $botDetails['aiid']);
            }
        }
        echo json_encode($tmp_published_list);
        unset($publishedBot);
        unset($tmp_published_list);
        ?>;
</script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, true, true]);
    </script>
</form>

</body>
</html>