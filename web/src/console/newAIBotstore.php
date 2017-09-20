<?php

namespace hutoma;

require_once __DIR__ . "/common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/common/bot.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

if (!isSessionVariablesAvailable()) {
    utils::redirect(config::getErrorPageUrl());
    exit;
}

$botApi = new api\botApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$purchasedBots = $botApi->getPurchasedBots();

$linkedBots = [];
if (isset(sessionObject::getCurrentAI()['aiid'])) {
    $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    $linkedBots = $aiApi->getLinkedBots(sessionObject::getCurrentAI()['aiid']);
    unset($aiApi);
}

$purchasedBotsList = [];
if (isset($purchasedBots) && (array_key_exists("bots", $purchasedBots))) {
    foreach ($purchasedBots['bots'] as $botDetails) {
        $purchasedBots = bot::fromObject($botDetails);
        $tmp_bot = $purchasedBots->toJSON();
        array_push($purchasedBotsList, $tmp_bot);
    }
}
unset($purchasedBots);


$linkedBotsList = [];
if (isset($linkedBots) && (array_key_exists("bots", $linkedBots))) {
    foreach ($linkedBots['bots'] as $botDetails) {
        array_push($linkedBotsList, $botDetails['botId']);
    }
}
unset($linkedBots);

$purchasedBotsJson = json_encode($purchasedBotsList);
$linkedBotsJson = json_encode($linkedBotsList);
unset($purchasedBotsList);
unset($linkedBotsList);


function isSessionVariablesAvailable()
{
    $ai = sessionObject::getCurrentAI();
    return (
        isset($ai['name']) &&
        isset($ai['description']) &&
        isset($ai['language']) &&
        isset($ai['timezone']) &&
        isset($ai['confidence']) &&
        isset($ai['personality']) &&
        isset($ai['voice'])
    );
}

$header_page_title = "Add Bot Skills";
$header_additional_entries = "<link rel=\"stylesheet\" href=\"/console/dist/css/switch.css\">";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper" id="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <div class="content-wrapper">
        <section class="content">
            <?php include __DIR__ . '/dynamic/newAIBotstore.content.html.php'; ?>

            <p id="botsCarousels"></p>
            <div class="row" style="background-color: #434343;">
                <div class="col-lg-12" style="background-color: #434343; padding:5px;">
                    <p></p>
                    <h2></h2>
                    <p id="botsSearch"></p>
                    <form method="POST" name="aiSkill">
                        <input type="hidden" name="userActivedBots" id="userActivedBots" value="" style="display:none;">
                    </form>
                </div>
            </div>
        </section>
    </div>
</div>

<?php include __DIR__ . '/include/page_footer_default.php'; ?>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>

<script src="<? $assets->getAsset('createAI/createAIWizard.js') ?>"></script>
<script src="<? $assets->getAsset('botstore/botstore.js') ?>"></script>
<script src="<? $assets->getAsset('botstore/carousel.js') ?>"></script>

<script src="<? $assets->getAsset('setting/setting.linkBot.js') ?>"></script>
<script src="<? $assets->getAsset('setting/setting.aiSkill.js') ?>"></script>
<script src="/console/dist/vendors/mustache.min.js"></script>
<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj("", "home", 0, false, true);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>

    var purchasedBots = <?php echo $purchasedBotsJson ?>;
    var linkedBots = <?php echo $linkedBotsJson ?>;

    function searchBots(str) {
        showAddSkills(str, purchasedBots, linkedBots);
    }

    $(document).ready(function () {
        activeRightMenu("<?php if (isset($_GET['botstore'])) echo json_decode($_GET['botstore']);?>");

        showAddSkills('', purchasedBots, linkedBots);
    });
</script>
</body>
</html>