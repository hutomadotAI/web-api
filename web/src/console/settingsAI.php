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

$aiid = getAiidOrRedirect();

$botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$purchasedBots = $botApi->getPurchasedBots();

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$aiInfo = $aiApi->getSingleAI($aiid);
$linkedBots = $aiApi->getLinkedBots($aiid);



function getAiidOrRedirect()
{
    if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
        \hutoma\console::redirect('./error.php?err=105');
        exit;
    }

    return $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
}

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Bot Settings</title>
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
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    
    <?php include_once "../console/common/google_tag_manager.php" ?>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
    <?php include_once "../console/common/google_tag_manager_no_js.php" ?>

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


                <div class="tab-content" style="padding-bottom:0px;">
                    <div class="tab-pane active" id="page_general">
                        <?php include './dynamic/settings.content.general.html.php'; ?>
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
<script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $aiInfo['name']; ?>", "settings", 1, false, false]);
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
                if ($botDetails['aiid'] !== $aiInfo['aiid'])
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
