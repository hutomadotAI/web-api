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
unset($aiList);
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

            if (!isset($aiListJson)) {
                include __DIR__ . '/dynamic/home.content.first.html.php';
                include __DIR__ . '/dynamic/home.content.start.html.php';
            }
            else {
                include __DIR__ . '/dynamic/home.content.start.html.php';
                include __DIR__ . '/dynamic/home.viewall.html.php';
            }
            ?>
        </section>
    </div>
<?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>
<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/datatables/jquery.dataTables.js"></script>
<script src="scripts/external/datatables/dataTables.bootstrap.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/home/home.js"></script>
<script src="./scripts/shared/shared.js"></script>

<script>
    var aiList = <?php echo $aiListJson ?>;
</script>


<?php
$menuObj = new menuObj("", "home", 0, false, true);
include __DIR__ . "/include/page_menu_builder.php" ?>

</body>
</html>