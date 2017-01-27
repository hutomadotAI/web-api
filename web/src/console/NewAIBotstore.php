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


if (isPostInputAvailable())
    setSessionVariablesFromPost();
else
    if (!isSessionVariablesAvailable()) {
        \hutoma\console::redirect('./error.php?err=100');
        exit;
    }


$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$bots = $botApi->getPublishedBots();
unset($botApi);

$botPurchaseApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$purchasedBots = $botPurchaseApi->getPurchasedBots();
unset($botPurchaseApi);


function isPostInputAvailable()
{
    return (
        isset($_POST['ai_name']) &&
        isset($_POST['ai_description']) &&
        isset($_POST['ai_language']) &&
        isset($_POST['ai_timezone']) &&
        isset($_POST['ai_confidence']) &&
        isset($_POST['ai_personality']) &&
        isset($_POST['ai_voice'])
    );
}

function setSessionVariablesFromPost()
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $_POST['ai_name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $_POST['ai_description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = $_POST['ai_language'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $_POST['ai_timezone'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $_POST['ai_confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $_POST['ai_personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $_POST['ai_voice'];
}

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
    <title>Hu:toma | Pre-trained NN </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

    <link rel="stylesheet" href="./plugins/iCheck/all.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
    <link rel="stylesheet" href="./plugins/star/star.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showBots('',0)">
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

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="plugins/createAI/createAIWizard.js"></script>
<script src="plugins/botstore/botstore.js"></script>
<script src="plugins/botstore/buyBot.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, true, true]);
    </script>
</form>
<script>
    var bots = <?php
        $tmp_list = [];
        if (isset($bots) && (array_key_exists("bots", $bots))) {
            foreach ($bots['bots'] as $botDetails) {
                $bot = new \hutoma\bot();
                $bot->setAiid($botDetails['aiid']);
                $bot->setAlertMessage($botDetails['alertMessage']);
                $bot->setBadge($botDetails['badge']);
                $bot->setBotId($botDetails['botId']);
                $bot->setCategory($botDetails['category']);
                $bot->setClassification($botDetails['classification']);
                $bot->setDescription($botDetails['description']);
                $bot->setLicenseType($botDetails['licenseType']);
                $bot->setLongDescription($botDetails['longDescription']);
                $bot->setName($botDetails['name']);
                $bot->setPrice($botDetails['price']);
                $bot->setPrivacyPolicy($botDetails['privacyPolicy']);
                $bot->setSample($botDetails['sample']);
                $bot->setVersion($botDetails['version']);
                $bot->setVideoLink($botDetails['videoLink']);
                
                $tmp_bot = $bot->toJSON();
                if ($botDetails['dev_id'] !== $_SESSION[$_SESSION['navigation_id']]['user_details']['dev_id'])
                    array_push($tmp_list, $tmp_bot);
            }
        }
        echo json_encode($tmp_list);
        unset($bots);
        unset($tmp_list);
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
        showBots(str, 0);
    }
</script>
</body>
</html>