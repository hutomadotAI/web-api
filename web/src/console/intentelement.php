<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/intentsApi.php";
require_once __DIR__ . "/api/entityApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    utils::redirect('./error.php?err=118');
    exit;
}

$intentName = isset($_POST['intent_name']) ? $_POST['intent_name'] : $_POST['intent'];
$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$entityList = $entityApi->getEntities();
unset($entityApi);

$intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$intent = $intentsApi->getIntent(sessionObject::getCurrentAI()['aiid'], $intentName);
unset($intentsApi);

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$bot= $aiApi->getSingleAI(sessionObject::getCurrentAI()['aiid']);
unset($aiApi);

if ($entityList['status']['code'] !== 200 && $entityList['status']['code'] !== 404) {
    unset($entityList);
    utils::redirect('./error.php?err=210');
    exit;
}

if ($intent['status']['code'] !== 200 && $intent['status']['code'] !== 404) {
    unset($intent);
    utils::redirect('./error.php?err=211');
    exit;
}

if ($bot['status']['code'] !== 200) {
    unset($bot);
    utils::redirect('./error.php?err=204');
    exit;
}

function isPostInputAvailable()
{
    return (isset($_POST['intent']) || isset($_POST['intent_name']));
}

function echoJsonIntentResponse($intent)
{
    if ($intent['status']['code'] !== 404)
        echo json_encode($intent);
    else
        echo '""'; // return empty string
}

function echoJsonEntityListResponse($entityList)
{
    if ($entityList['status']['code'] !== 404) {
        echo json_encode($entityList['entities']);
    } else
        echo '""'; // return empty string
}

$header_page_title = "Edit Intent";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12" id="intentElementBox">
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <?php include __DIR__ . '/dynamic/intent.element.content.head.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.element.content.expression.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.element.content.variable.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.element.content.response.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.element.content.webhook.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.element.content.prompt.html.php'; ?>
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

<script src="scripts/external/jQuery/jquery.omniselect.js"></script>
<script src="scripts/external/saveFile/FileSaver.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/button-select/buttonSelect.js"></script>
<script src="./scripts/intent/intent.polling.js"></script>
<script src="./scripts/intent/intent.element.response.js"></script>
<script src="./scripts/intent/intent.element.webhook.js"></script>
<script src="./scripts/intent/intent.element.expression.js"></script>
<script src="./scripts/intent/intent.element.js"></script>
<script src="./scripts/intent/intent.element.prompt.js"></script>
<script src="./scripts/intent/intent.element.variable.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="scripts/external/saveFile/FileSaver.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "intents", 1, false, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>
    var entityListFromServer = <?php echo echoJsonEntityListResponse($entityList); unset($entityList);?>;
    var intent = <?php echoJsonIntentResponse($intent); unset($intent);?>;
    var trainingFile = <?php if ($bot['training_file_uploaded']) echo 'true'; else echo 'false'; unset($bot)?>;
    var data_changed = false;
</script>
</body>
</html>
