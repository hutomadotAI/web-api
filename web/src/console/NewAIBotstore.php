<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "common/bot.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isSessionVariablesAvailable()) {
    \hutoma\console::redirect('./error.php?err=100');
    exit;
}

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$bots = $botApi->getPublishedBots();

$botPurchaseApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$purchasedBots = $botPurchaseApi->getPurchasedBots();
unset($botPurchaseApi);

function isSessionVariablesAvailable()
{
    return (
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'])
    );
}

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Add skills </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

    <link rel="stylesheet" href="scripts/external/iCheck/all.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<div class="wrapper" id="wrapper">
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
            <?php include './dynamic/newAIBotstore.content.html.php'; ?>
            <?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

</div>

<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, true, true]);
    </script>
</form>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="scripts/createAI/createAIWizard.js"></script>
<script src="scripts/botstore/botstore.js"></script>
<script src="scripts/botstore/buyBot.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script>
    var bots = <?php
        $tmp_list = [];
        if (isset($bots) && (array_key_exists("bots", $bots))) {
            foreach ($bots['bots'] as $botDetails) {
                $bot = \hutoma\bot::fromObject($botDetails);
                $tmp_bot = $bot->toJSON();
                array_push($tmp_list, $tmp_bot);
            }
        }
        echo json_encode($tmp_list);
        unset($bots);
        unset($tmp_list);
        unset($botApi);
        ?>;

    var purchasedBots = <?php
        $tmp_purchased_list = [];
        if (isset($purchasedBots) && (array_key_exists("bots", $purchasedBots))) {
            foreach ($purchasedBots['bots'] as $botDetails) {
                $purchasedBot = new \hutoma\bot();
                array_push($tmp_purchased_list, $botDetails['botId']);
            }
        }
        echo json_encode($tmp_purchased_list);
        unset($purchasedBot);
        unset($tmp_purchased_list);
        ?>;
</script>
<script>
    var newNode = document.createElement('div');
    newNode.className = 'row no-margin';
    newNode.id = 'bot_list';

    function searchBots(str) {
        showBots(str, DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value,bots,purchasedBots);
    }
</script>
<script>
    $(document).ready(function () {
        showBots('', DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value,bots,purchasedBots);
    });
</script>
</body>
</html>