<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "common/bot.php";
require_once "api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

if (!isSessionVariablesAvailable()) {
    \hutoma\console::redirect('./error.php?err=105');
    exit;
}

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$purchasedBots = $botApi->getPurchasedBots();

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$linkedBots = $aiApi->getLinkedBots($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($aiApi);


function isSessionVariablesAvailable()
{
    return (
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']) &&
        isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'])
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
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="scripts/external/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="scripts/external/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet" href="scripts/external/iCheck/all.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
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

            <!-- Custom Tabs -->
            <div class="nav-tabs-custom flat no-shadow no-border">
                <ul class="nav nav-tabs">
                    <li class="active" id="tab_general"><a href="#page_general" data-toggle="tab">General</a></li>
                    <li id="tab_aiskill" id="tab_aiskill"><a href="#page_aiskill" data-toggle="tab">Bot Skills</a></li>
                </ul>

                <div class="tab-content" style="padding-bottom:0px;">
                    <!-- GENERAL TAB -->
                    <div class="tab-pane active" id="page_general">
                        <?php include './dynamic/settings.content.general.html.php'; ?>
                    </div>

                    <!-- BOT LINK TAB -->
                    <div class="tab-pane" id="page_aiskill">
                        <?php include './dynamic/settings.content.aiSkill.html.php'; ?>
                        <div class="row" style="background-color: #434343;">
                            <div class="col-lg-12" style="background-color: #434343; padding:5px;">
                                <?php include './dynamic/settings.content.aiSkill.list.html.php'; ?>
                            </div>
                        </div>
                    </div>

                </div>

        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/inputCommon/inputCommon.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/deleteAI/deleteAI.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>
<script src="scripts/external/bootstrap-slider/bootstrap-slider.js"></script>
<script src="scripts/external/ionslider/ion.rangeSlider.min.js"></script>

<script src="./scripts/clipboard/copyToClipboard.js"></script>
<script src="./scripts/clipboard/clipboard.min.js"></script>

<script src="./scripts/setting/setting.linkBot.js"></script>
<script src="./scripts/setting/setting.general.js"></script>
<script src="./scripts/setting/setting.aiSkill.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "settings", 1, false, false]);
    </script>
    <script>
        $(document).ready(function () {
            activeRightMenu("<?php if (isset($_GET['botstore'])) echo json_decode($_GET['botstore']);?>");
        });
    </script>
</form>
<script>
    var purchasedBots = <?php
        $tmp_list = [];
        if (isset($purchasedBots) && (array_key_exists("bots", $purchasedBots))) {
            foreach ($purchasedBots['bots'] as $botDetails) {
                $purchasedBots = \hutoma\bot::fromObject($botDetails);
                $tmp_bot = $purchasedBots->toJSON();
                if ($botDetails['aiid'] !== $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])
                    array_push($tmp_list, $tmp_bot);
            }
        }
        echo json_encode($tmp_list);
        unset($purchasedBots);
        unset($tmp_list);
        unset($botApi);
        ?>;


    var linkedBots = <?php
        $tmp_linked_list = [];
        if (isset($linkedBots) && (array_key_exists("bots", $linkedBots))) {
            foreach ($linkedBots['bots'] as $botDetails) {
                $linkedBot = new \hutoma\bot();
                array_push($tmp_linked_list, $botDetails['botId']);
            }
        }
        echo json_encode($tmp_linked_list);
        unset($purchasedBots);
        unset($tmp_linked_list);
        ?>;
</script>

<script>
    var newNode = document.createElement('div');
    newNode.className = 'row no-margin';
    newNode.id = 'bot_list';
    function searchBots(str) {
        showAddSkills(str, purchasedBots,linkedBots);
    }
</script>
<script>
    $(document).ready(function () {
        showAddSkills('', purchasedBots,linkedBots);
    });
</script>
</body>
</html>
