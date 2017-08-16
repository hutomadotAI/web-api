<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/intentsApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

$aiid = sessionObject::getCurrentAI()['aiid'];
$intent_deleted = false;

$intents = $intentsApi->getIntents($aiid);
unset($intentsApi);

if ($intents['status']['code'] !== 200 && $intents['status']['code'] !== 404) {
    unset($intents);
    utils::redirect('./error.php?err=210');
    exit;
}

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$bot= $aiApi->getSingleAI($aiid);
unset($aiApi);

if ($bot['status']['code'] !== 200) {
    unset($bot);
    utils::redirect('./error.php?err=204');
    exit;
}

function echoJsonIntentsResponse($intents)
{
    if ($intents['status']['code'] !== 404)
        echo json_encode($intents['intent_name']);
    else
        echo '""'; // return empty string
}

$header_page_title = "Intents";
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
                    <?php include __DIR__ . '/dynamic/intent.content.create.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/intent.content.list.html.php'; ?>
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

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/intent/intent.polling.js"></script>
<script src="./scripts/intent/intent.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "intents", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


<script>
    var intents = <?php echoJsonIntentsResponse($intents); unset($intents); ?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'intents_list';
    
    var intent_deleted = <?php echo ($intent_deleted ? 'true' : 'false'); unset($intent_deleted)?>;
    var ai_state = <?php echo json_encode($bot['ai_status'])?>;
    var trainingFile = <?php echo ($bot['training_file_uploaded'] ? 'true' : 'false'); unset($bot)?>;

    function searchIntents(str) {
        showIntents(str);
    }

    $(document).ready(function () {
        showIntents('');
    });
</script>
</body>
</html>