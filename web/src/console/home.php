<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/adminApi.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botApi.php";
require_once __DIR__ . "/common/bot.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/common/config.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

// This page is loaded after login, make sure we store any user information we're going to need later
sessionObject::populateSessionWithUserDetails(sessionObject::getCurrentUsername());

// Obtain the AI list
$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$response_getAIs = $aiApi->getAIs();
$aiList = [];
if (isset($response_getAIs) && (array_key_exists("ai_list", $response_getAIs))) {
    $aiList = $response_getAIs['ai_list'];
}
$aiListJson = json_encode($aiList);
unset($response_getAIs);
unset($aiApi);

$header_page_title = "Home";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>
    <div class="content-wrapper">
        <section class="content">
            <?php

            if (empty($aiList)) {
                include __DIR__ . '/dynamic/home.content.first.html.php';
                include __DIR__ . '/dynamic/home.content.start.html.php';
            }
            else {
                include __DIR__ . '/dynamic/home.content.start.html.php'; ?>
                <div class="box box-solid box-clean flat no-shadow unselectable">
                    <div class="box-header with-border">
                        <div class="box-title"><b>Your Bots</b></div>
                    </div>

                    <div class="box-body table-responsive no-border" style="padding-top:0;">
                        <table class="table dataTable" id="tableAi">
                            <thead>
                            <tr disabled>
                                <th class="text-left" style="border:0; width:20%">Bot Name</th>
                                <th class="text-left" style="border:0; width:25%">Description</th>
                                <th class="text-center" style="border:0; width:15%">Status</th>
                                <th style="border:0; width:5%"></th>
                                <th style="border:0; width:5%"></th>
                            </tr>
                            </thead>
                            <tbody id="tableAiList">
                            </tbody>
                        </table>

                        <form method="POST" name="viewAllForm" action="">
                            <input type="hidden" id="ai" name="ai" value="">
                        </form>
                    </div>

                <?php
                }
                ?>
        </section>
    </div>
    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>
<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/mustache.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>
<script src="<?php $assets->getAsset('home/home.js') ?>"></script>
<script src="<?php $assets->getAsset('shared/shared.js') ?>"></script>

<script>
    var aiList = <?php echo $aiListJson ?>;

    $(document).ready(function () {
        drawTableRows();
    });
</script>


<?php
$menuObj = new menuObj("", "home", 0, false, true);
include __DIR__ . "/include/page_menu_builder.php";
?>

</body>
</html>