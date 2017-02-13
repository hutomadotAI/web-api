<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "common/bot.php";
require_once "common/developer.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

// TODO temporary removed - it block the visualization of Botstore during creation Ai processs
/*
if (!isAuthorizedToAccess()) {
    \hutoma\console::redirect('./error.php?err=105');
    exit;
}
*/

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$bots = $botApi->getPublishedBots();

$botPurchaseApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$purchasedBots = $botPurchaseApi->getPurchasedBots();
unset($botPurchaseApi);

function isAuthorizedToAccess()
{
    return (
    isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])
    );
}
?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Botstore </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./plugins/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
    <link rel="stylesheet" href="./plugins/star/star.css">
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
            <?php include './dynamic/botstore.content.html.php'; ?>
            <?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>
        </section>
    </div>
</div>

<footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
</footer>
</div>

<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "botstore", 2, true, false]);
    </script>
</form>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/select2/select2.full.js"></script>

<script src="./plugins/botstore/botstoreWizard.js"></script>
<script src="./plugins/botstore/botstore.js"></script>
<script src="./plugins/botstore/buyBot.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>



<script>
    var bots = <?php
        $tmp_list = [];
        if (isset($bots) && (array_key_exists("bots", $bots))) {
            foreach ($bots['bots'] as $botDetails) {
                $bot = \hutoma\bot::fromObject($botDetails);
                $tmp_bot = $bot->toJSON();
                if ($botDetails['dev_id'] !== $_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'])
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
    newNode.className = 'row';
    newNode.id = 'bot_list';

    function searchBots(str) {
        showBots(str, 1);
    }
</script>
<script>
    $(document).ready(function () {
        showBots('', 1);
    });

</script>
</body>
</html>