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



if(!isset($_SESSION[$_SESSION['navigation_id']]['user_details'])){
    $api = new \hutoma\api\adminApi(\hutoma\console::isLoggedIn(), \hutoma\config::getAdminToken());
    $userInfo = $api->getUserInfo($_SESSION['navigation_id']);

    // dial the amount of session information right back - we really don't want secrets stored
    $_SESSION[$_SESSION['navigation_id']]['user_details']['name'] = $userInfo['name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['username'] = $userInfo['username'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'] = $userInfo['dev_id'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['user_joined'] = \hutoma\console::joinedSince($userInfo['created']);
    $_SESSION[$_SESSION['navigation_id']]['user_details']['created'] = $userInfo['created'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['email'] = $userInfo['email'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['id'] = $userInfo['id'];

    unset($api);
    unset($userInfo);
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
    <title>Hu:toma | Home</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="scripts/external/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    <script src="scripts/external/autopilot/autopilot.js"></script>
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
<script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>

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