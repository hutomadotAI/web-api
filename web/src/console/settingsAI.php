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
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

$aiid = sessionObject::getCurrentAI()['aiid'];

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$aiInfo = $aiApi->getSingleAI($aiid);

$header_page_title = "Bot Settings";
$header_additional_entries = "<link rel=\"stylesheet\" href=\"/console/dist/vendors/ionslider/ion.rangeSlider.css\">\n
    <link rel=\"stylesheet\" href=\"/console/dist/vendors/ionslider/ion.rangeSlider.skinNice.css\">";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>
    <div class="content-wrapper">
        <section class="content">
            <div class="tab-content" style="padding-bottom:0px;">
                <div class="tab-pane active" id="page_general">
                    <?php include __DIR__ . '/dynamic/settings.content.general.html.php'; ?>
                </div>
            </div>
        </section>
    </div>

    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>

<script src="<? $assets->getAsset('inputCommon/inputCommon.js') ?>"></script>
<script src="<? $assets->getAsset('validation/validation.js') ?>"></script>
<script src="<? $assets->getAsset('deleteAI/deleteAI.js') ?>"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>
<script src="/console/dist/vendors/bootstrap-slider/bootstrap-slider.js"></script>
<script src="/console/dist/vendors/ionslider/ion.rangeSlider.min.js"></script>

<script src="<? $assets->getAsset('clipboard/copyToClipboard.js') ?>"></script>
<script src="<? $assets->getAsset('clipboard/clipboard.min.js') ?>"></script>

<script src="<? $assets->getAsset('setting/setting.general.js') ?>"></script>

<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "settings", 1, false, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


</body>
</html>
