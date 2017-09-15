<?php

namespace hutoma;

require_once __DIR__ . "/common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/integrationApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$integrationApi = new api\integrationApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

if (!isset(sessionObject::getCurrentAI()['aiid'])) {
    errorRedirect::defaultErrorRedirect();
    exit;
}
$aiid = sessionObject::getCurrentAI()['aiid'];

if (isset($_GET["code"])) {
    $connect_token = $_GET["code"];
    $redir = $_COOKIE["facebookRedir"];
    $connect_result = $integrationApi->setConnectToken($aiid, $connect_token, $redir);
    $_SESSION[$_SESSION['navigation_id']]['fb_connect_result'] = $connect_result;
    utils::redirect($redir);
    exit();
}

$header_page_title = "Bot Integrations";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow">
                        <?php include __DIR__ . '/dynamic/integrations.facebook.html.php'; ?>
                    </div>
                </div>
            </div>
        </section>
    </div>
    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./scripts/validation/validation.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./dist/js/mustache.min.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "integrations", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

</body>
</html>
