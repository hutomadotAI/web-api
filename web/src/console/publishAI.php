<?php

namespace hutoma;

require_once __DIR__ . "/common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/api/developerApi.php";
require_once __DIR__ . "/common/Assets.php";

$assets = new Assets();

sessionObject::redirectToLoginIfUnauthenticated();

if (isset($_POST['ai'])) {
    getBasicAiInfo($_POST['ai']);
}

function getBasicAiInfo($aiid){
    $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);
    unset($aiApi);

    if ($singleAI['status']['code'] !== 200 && $singleAI['status']['code'] !== 404) {
        $singleAI_result = $singleAI;
        unset($singleAI);
        errorRedirect::handleErrorRedirect($singleAI_result);
        exit;
    }
    unset($singleAI);
}

$header_page_title = "Publish Bot";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <?php include __DIR__ . '/dynamic/publishAI.content.html.php'; ?>
                </div>
            </div>
        </section>
    </div>
    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap-filestyle.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>

<script src="<? $assets->getAsset('validation/validation.js') ?>"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>
<script src="/console/dist/vendors/bootstrap-slider/bootstrap-slider.js"></script>

<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>
<script>
    $(function () {
        $('.select2').select2();
    });

</script>

<?php
$menuObj = new menuObj("", "home", 0, false, true);
include __DIR__ . "/include/page_menu_builder.php" ?>


</body>
</html>