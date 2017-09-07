<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/common/bot.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$aiid = sessionObject::getCurrentAI()['aiid'];

$botApi = new api\botApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$purchasedBots = $botApi->getPurchasedBots();
unset($botApi);

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$aiInfo = $aiApi->getSingleAI($aiid);
$linkedBots = $aiApi->getLinkedBots($aiid);
unset($aiApi);

$purchasedBotsList = [];
if (isset($purchasedBots) && (array_key_exists("bots", $purchasedBots))) {
    foreach ($purchasedBots['bots'] as $botDetails) {
        $purchasedBots = bot::fromObject($botDetails);
        $tmp_bot = $purchasedBots->toJSON();
        if ($botDetails['aiid'] !== $aiInfo['aiid'])
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

$header_page_title = "Add Skill";
$header_additional_entries = "<link rel=\"stylesheet\" href=\"./dist/css/switch.css\">";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

    <div class="content-wrapper">
        <?php include __DIR__ . "/include/page_header_default.php"; ?>
        <section class="content">
            <div class="tab-content" style="padding-bottom:0px;">
                <?php include __DIR__ . '/dynamic/settings.content.aiSkill.html.php'; ?>
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
            </div>
        </section>
    </div>
<?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/inputCommon/inputCommon.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>
<script src="scripts/external/bootstrap-slider/bootstrap-slider.js"></script>
<script src="scripts/external/ionslider/ion.rangeSlider.min.js"></script>
<script src="./dist/js/mustache.min.js"></script>
<script src="./scripts/setting/setting.linkBot.js"></script>
<script src="./scripts/setting/setting.aiSkill.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
$menuObj = new menuObj($aiInfo['name'], "skill", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>
    var purchasedBots = <?php echo $purchasedBotsJson ?>;
    var linkedBots = <?php echo $linkedBotsJson ?>;

    function searchBots(str) {
        showAddSkills(str, purchasedBots,linkedBots);
    }

    $(document).ready(function () {
        showAddSkills('', purchasedBots,linkedBots);
    });
</script>
</body>
</html>
