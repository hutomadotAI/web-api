<?php
require "../pages/config.php";
require_once "./api/apiBase.php";
require_once "./api/entityApi.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "api/developerApi.php";
require_once "common/bot.php";
require_once "common/developer.php";

if(!\hutoma\console::checkSessionIsActive()){
     exit;
}

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['botid'])) {
    \hutoma\console::redirect('./error.php?err=100');
    exit;
}

$botId = $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['botid'];
$purchased = $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['purchased'];
$menu_title = $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['menu_title'];
$name = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'];

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$botDetails = $botApi->getBotDetails($botId);
unset($botId);
unset($botApi);

$devInfoApi = new \hutoma\api\developerApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$devInfo = $devInfoApi->getDeveloperInfo($botDetails['bot']['dev_id']);
unset($devInfoApi);

if (isset($botDetails)) {
    switch ($botDetails['status']['code']) {
        case 200:
            break;
        case 404:
            \hutoma\console::redirect('./error.php?err=100');
            exit;
        case 500:
            \hutoma\console::redirect('./error.php?err=100');
            exit;
    }
}
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Botstore - box</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
</head>
<script>
    var bot = <?php
        $bot = new \hutoma\bot();
        if (isset($botDetails) && (array_key_exists('bot', $botDetails))) {
            $bot = \hutoma\bot::fromObject($botDetails['bot']);
        }
        $tmp_bot = $bot->toJSON();
        unset($bot);

        echo json_encode($tmp_bot);
        unset($tmp_bot);
        ?>;

    var devInfo = <?php
    $dev = new \hutoma\developer();
    $dev->setCompany($devInfo['info']['company']);
    $dev->setWebsite($devInfo['info']['website']);
    echo json_encode($dev->toJSON());
    unset($dev);
    unset($devInfo);
    ?>
</script>

<body class="hold-transition skin-blue fixed sidebar-mini" style="background:#2c3b41;">
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
            <div class="row">

                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">
                        <?php
                        include './dynamic/botstore.content.singleBot.card.html.php';
                        include './dynamic/botstore.content.singleBot.video.html.php';
                        include './dynamic/botstore.content.singleBot.description.html.php';
                        include './dynamic/botstore.content.singleBot.footer.html.php';
                        ?>
                    </div>
                    <?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>
                    <script src="./scripts/botcard/botcard.js"></script>
                    <script src="./scripts/botstore/botstoreWizard.js"></script>
                </div>
            </div>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/botcard/buyBot.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        var info = infoForBotstore("<?php echo $menu_title; unset($menu_title)?>", "<?php echo $purchased; unset($purchased);?>");
        MENU.init(["<?php echo $name; unset($name); ?>", info['menu_title'], info['menu_level'], info['menu_block'], info['menu_active']]);
    </script>
</form>
<script>
    populateBotFields(bot);
</script>
</body>
</html>
