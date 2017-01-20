<?php
require "../pages/config.php";
require_once "./api/apiBase.php";
require_once "./api/entityApi.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "common/bot.php";
require_once "common/developer.php";


if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_POST['botId'])) {
    \hutoma\console::redirect('./error.php?err=100');
    exit;
}

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$botDetails = $botApi->getBotDetails($_POST['botId']);
unset($botApi);


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


function isPostCameFromBotstore()
{
    return (
        isset($_POST['menu_title'])&&
        isset($_POST['menu_level']) &&
        isset($_POST['menu_block']) &&
        isset($_POST['menu_active']) &&
        isset($_POST['menu_deep'])
    );
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Botstore - box</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
    <link rel="stylesheet" href="./plugins/star/star.css">
</head>
<script>
    var bot = <?php
        $bot = new \hutoma\bot();
        if (isset($botDetails) && (array_key_exists('bot', $botDetails))) {
            // TODO probably this value is hidden to client side
            //$bot->setAiid($botDetails['bot']['aiid']);
            $bot->setAlertMessage($botDetails['bot']['alertMessage']);
            $bot->setBadge('Top Developer');                        //$botDetails['bot']['badge']);
            $bot->setBotId($botDetails['bot']['botId']);
            $bot->setCategory($botDetails['bot']['category']);
            $bot->setClassification($botDetails['bot']['classification']);
            $bot->setDescription($botDetails['bot']['description']);
            $bot->setLicenseType($botDetails['bot']['licenseType']);
            $bot->setUpdate($botDetails['bot']['lastUpdate']);
            $bot->setLongDescription($botDetails['bot']['longDescription']);
            $bot->setImagePath('');
            $bot->setName($botDetails['bot']['name']);
            $bot->setPrice($botDetails['bot']['price']);
            $bot->setPrivacyPolicy($botDetails['bot']['privacyPolicy']);
            $bot->setSample($botDetails['bot']['sample']);
            $bot->setUsers('103');                                  //$botDetails['bot']['users']);
            $bot->setRating('4.3');                                   //$botDetails['bot']['rating']);
            $bot->setActivations($bot->rangeActivation($bot->getUsers()));
            $bot->setVersion($botDetails['bot']['version']);
            $bot->setVideoLink($botDetails['bot']['videoLink']);
        }
        $tmp_bot = $bot->toJSON();
        unset($bot);

        echo json_encode($tmp_bot);
        unset($tmp_bot);
        ?>;
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
                    <script src="./plugins/botstore/botstoreWizard.js"></script>
                </div>
            </div>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./plugins/botstore/buyBot.js"></script>
<script src="./dist/js/app.min.js"></script>


<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        var info = infoForBotstore("<?php if(isset($_POST['menu_title'])) echo ($_POST['menu_title']);?>","<?php if(isset($_POST['purchased'])) echo ($_POST['purchased']);?>");
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", info['menu_title'], info['menu_level'], info['menu_block'], info['menu_active']]);
    </script>
</form>
<script>
    populateBotFields(bot);
</script>
</body>
</html>
