<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";

$assets = new Assets();

sessionObject::redirectToLoginIfUnauthenticated();

function isPreviousFieldsFilled() {
    $ai = sessionObject::getCurrentAI();
    return  (
        isset($ai['name']) &&
        isset($ai['description']) &&
        isset($ai['language']) &&
        isset($ai['timezone']) &&
        isset($ai['confidence']) &&
        isset($ai['personality']) &&
        isset($ai['voice'])
    );
}


$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response_getAIs = $aiApi->getAIs();
unset($aiApi);

$name_list='';
if (isset($response_getAIs) && (array_key_exists("ai_list",$response_getAIs))) {
    for ($i = 0, $l = count($response_getAIs['ai_list']); $i < $l; ++$i)
        $name_list[$i] = $response_getAIs['ai_list'][$i]['name'];
}

unset($response_getAIs);


$header_page_title = "Create Bot";
$header_additional_entries = "<link rel=\"stylesheet\" href=\"/console/dist/vendors/ionslider/ion.rangeSlider.css\">
    <link rel=\"stylesheet\" href=\"/console/dist/vendors/ionslider/ion.rangeSlider.skinNice.css\">";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <div class="content-wrapper">
        <section class="content">
            <?php include __DIR__ . '/dynamic/newAI.content.html.php'; ?>
        </section>
    </div>

    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script>
    var name_list = <?php echo json_encode($name_list); unset($name_list);?>;
    var previousFilled = <?php if (isPreviousFieldsFilled()) echo('true'); else echo ('false'); ?>;
    var previousGeneralInfo  = <?php if (isPreviousFieldsFilled()) echo json_encode($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']); else echo 'false';?>;
    var err = <?php if(isset($_GET['err'])) echo($_GET['err']); else echo ('false'); ?>;
    var errObj = <?php if(isset($_GET['errObj'])) echo($_GET['errObj']); else echo json_encode('');?>;
</script>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>

<script src="<? $assets->getAsset('inputCommon/inputCommon.js') ?>"></script>
<script src="<? $assets->getAsset('validation/validation.js') ?>"></script>
<script src="<? $assets->getAsset('createAI/createAI.js') ?>"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>
<script src="/console/dist/vendors/bootstrap-slider/bootstrap-slider.js"></script>
<script src="/console/dist/vendors/ionslider/ion.rangeSlider.min.js"></script>

<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj("", "home", 0, false, true);
include __DIR__ . "/include/page_menu_builder.php" ?>

</body>
</html>