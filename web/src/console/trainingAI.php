<?php

namespace hutoma;

require_once __DIR__ . "common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$aiid = isset($_REQUEST['ai']) ? $_REQUEST['ai'] : sessionObject::getCurrentAI()['aiid'];

if (!isset($aiid)) {
    errorRedirect::defaultErrorRedirect();
    exit;
}

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$ai = $aiApi->getSingleAI($aiid);
unset($aiApi);
if ($ai['status']['code'] === 200) {
    sessionObject::populateCurrentAI($ai);
    $deepLearningError = json_encode($ai['deep_learning_error']);
    $aiStatus = json_encode($ai['ai_status']);
    $phase1progress = json_encode($ai['phase_1_progress']);
    $phase2progress = json_encode($ai['phase_2_progress']);
    $trainingFileUploaded = json_encode($ai['training_file_uploaded']);
} else {
    $ai_result = clone $ai;
    unset($ai);
    errorRedirect::handleErrorRedirect($ai_result);
    exit;
}
unset($singleAI);

$header_page_title = "Bot Training";
include __DIR__ . "/include/page_head_default.php";
$body_additional_style = "margin-right:350px;";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<script>
    var deep_error = <?php echo $deepLearningError ?>;
    var aiStatus = {
        "ai_status": <?php echo $aiStatus ?>,
        "phase_1_progress": <?php echo $phase1progress ?>,
        "phase_2_progress": <?php echo $phase2progress ?>,
        "deep_learning_error": <?php echo $deepLearningError ?>,
        "training_file_uploaded": <?php echo $trainingFileUploaded ?>
    };

</script>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12" id="trainingBox">
                    <div class="alert alert-dismissable flat alert-info unselectable" id="containerMsgAlertTrainingInfo" style="padding-bottom: 25px;">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                        <span id="msgAlertTrainingInfo" class="text-muted" >
                            <dt>How do you train a bot?</dt>
                            <dl class="dl-horizontal no-margin" style="text-align:justify">
                                You can use our platform to upload text format training data that you may have. For example,
                                existing customer service chat history or other conversational content stored in your CRM, emails or
                                chat platforms. Once you upload the file, our system will process it and you can further train it by
                                creating more questions and answers.
                            </dl>
                        </span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <?php include __DIR__ . '/dynamic/training.content.upload.file.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/training.content.monitor.html.php'; ?>
                </div>
            </div>
        </section>
    </div>
    <!-- ================ CHAT CONTENT ================= -->
    <aside class="control-sidebar control-sidebar-dark control-sidebar-open">
        <?php include __DIR__ . '/dynamic/chat.html.php'; ?>
        <?php include __DIR__ . '/dynamic/training.content.json.html.php'; ?>
    </aside>
    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./bootstrap/js/bootstrap-filestyle.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/messaging/messaging.js"></script>
<script src="scripts/external/iCheck/icheck.min.js"></script>
<script src="./scripts/training/training.area.upload.textfile.js"></script>
<script src="./scripts/training/training.area.js"></script>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>
<script src="./scripts/clipboard/copyToClipboard.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "training", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


</body>
</html>
