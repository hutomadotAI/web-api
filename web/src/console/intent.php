<?php

namespace hutoma;

require_once __DIR__ . "/common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/aiApi.php";
require_once __DIR__ . "/api/intentsApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

$intentsApi = new api\intentsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

$aiid = sessionObject::getCurrentAI()['aiid'];
$intent_deleted = false;
$currentIntent = isset($_POST['intent']) ? $_POST['intent'] : "";

$intents = $intentsApi->getIntents($aiid);
unset($intentsApi);

$intents_status = $intents['status']['code'];
if ($intents_status !== 200 && $intents_status !== 404) {
        $intents_result = $intents;
    unset($intents);
    errorRedirect::handleErrorRedirect($intents_result);
    exit;
}

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$bot = $aiApi->getSingleAI($aiid);
unset($aiApi);

$bot_status = $bot['status']['code'];
if ($bot_status !== 200 && $bot_status !== 404) {
    $bot_result = $bot;
    unset($bot);
    errorRedirect::handleErrorRedirect($bot_result);
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
                    <div class="box box-solid box-clean flat no-shadow unselectable">

                        <div class="box-header with-border">
                            <i class="fa fa-commenting-o text-green"></i>
                            <div class="box-title"><b>New Intent</b></div>
                            <a data-toggle="collapse" href="#collapseIntentsInfo">
                                <div class=" pull-right">more info
                                    <i class="fa fa-info-circle text-sm text-yellow"></i>
                                </div>
                            </a>
                        </div>

                        <div id="collapseIntentsInfo" class="panel-collapse collapse">
                            <div class="box-body" style="padding-bottom:0;">
                                <div class="overlay center-block">
                                    <section class="content-info">
                                        <div class="box-body">
                                            <dl class="dl-horizontal no-margin" style="text-align:justify">
                                                Let’s say you’re creating a Bot that takes orders in a bar. A user may
                                                say "I want to order" - this is their intent. The associated entities
                                                could be "beer" "cola" or “wine".
                                            </dl>
                                        </div>
                                    </section>
                                </div>
                            </div>
                        </div>

                        <div class="box-body" id="boxIntents">
                            <div class="bootstrap-filestyle input-group" id="GrpIntentButton">
                                <input type="text" class="form-control flat no-shadow" id="inputIntentName"
                                       name="intent" placeholder="Give the intent a name" style="width: 96%;"
                                       onkeyup="checkIntentCode(this,event.keyCode)">
                                <div class="input-group-btn" tabindex="0">
                                    <button id="btnCreateIntent" class="btn btn-success flat" style="width: 120px;">
                                        Create Intent
                                    </button>
                                </div>
                            </div>
                            <p></p>

                            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntent"
                                 style="margin: 0 0 10px 0;">
                                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                                <i class="icon fa fa-check" id="iconAlertIntent"></i>
                                <span id="msgAlertIntent"></span>
                            </div>
                        </div>

                        <div class="box-footer">
        <span>
            If you’re stuck check out our <a data-toggle="collapse"
                                             href="#collapseVideoTutorialIntent">intents tutorial</a> or email <a
                    href='mailto:support@hutoma.ai?subject=Invite%20to%20slack%20channel' tabindex="-1">support@hutoma.ai</a> for an invite to our slack channel.
        </span>
                            <p></p>

                            <div id="collapseVideoTutorialIntent" class="panel-collapse collapse">
                                <div class="box-body flat no-padding">
                                    <div class="overlay center-block">
                                        <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                                            <iframe
                                                    src="//www.youtube.com/embed/SI5XgQm660A?controls=1&hd=1&enablejsapi=1"
                                                    frameborder="0" allowfullscreen>
                                            </iframe>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>


                        <input type="hidden" id="intent-name" name="intent-name" value="<?php echo $currentIntent ?>">
                        <input type="hidden" id="bot-status" name="bot-status" value="0" style="display:none;"/>
                        <div class="box box-solid box-clean flat no-shadow unselectable">

                            <div class="box-header with-border">
                                <i class="fa fa-commenting-o text-green"></i>
                                <div class="box-title"><b>Intent List</b></div>
                                <a data-toggle="collapse" href="#collapseIntentsListInfo">
                                    <div class=" pull-right">more info
                                        <i class="fa fa-info-circle text-sm text-yellow"></i>
                                    </div>
                                </a>
                            </div>

                            <div id="collapseIntentsListInfo" class="panel-collapse collapse">
                                <div class="box-body" style="padding-bottom: 0px;">
                                    <div class="overlay center-block">
                                        <section class="content-info">
                                            <div class="box-body">
                                                <dl class="dl-horizontal no-margin" style="text-align:justify">
                                                    This is a list of intents that you have created for your bot so far.
                                                    These are unique to this Bot.
                                                </dl>
                                            </div>
                                        </section>
                                    </div>
                                </div>
                            </div>

                            <div class="box-body">

                                <div class="input-group-btn">
                                    <input class="form-control flat no-shadow pull-right"
                                           onkeyup="searchIntents(this.value)" value="" placeholder="Search...">
                                </div>

                                <p></p>

                                <div class="tab-pane" id="tab_intents">
                                    <p id="intentsearch"></p>
                                </div>
                                <p></p>
                            </div>

                        </div>


                        <!-- Modal DELETE entity-->
                        <div class="modal fade" id="deleteIntent" role="dialog">
                            <div class="modal-dialog flat width600">
                                <!-- Modal content-->
                                <div class="modal-content bordered" style="background-color: #202020">
                                    <div class="modal-header">
                                        <button type="button" class="close text-gray" data-dismiss="modal">&times;
                                        </button>
                                        <h4 class="modal-title">Delete Intent</h4>
                                    </div>
                                    <div class="modal-body" style="background-color: #212121">
                                        <div class="box-body" id="delete-intent-label">

                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel
                                        </button>
                                        <button type="button" class="btn btn-danger flat" id="modalDelete" value=""
                                                onClick="deleteIntent(this.value)" data-dismiss="modal">Delete
                                        </button>
                                    </div>
                                </div>

                            </div>
                        </div>
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
<script src="/console/dist/vendors/mustache.min.js"></script>
<script src="<?php $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<?php $assets->getAsset('validation/validation.js') ?>"></script>
<script src="<?php $assets->getAsset('intent/intent.polling.js') ?>"></script>
<script src="<?php $assets->getAsset('intent/intent.js') ?>"></script>
<script src="<?php $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "intents", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


<script>
    var intents = <?php echoJsonIntentsResponse($intents); unset($intents); ?>;

    var intent_deleted = <?php echo($intent_deleted ? 'true' : 'false'); unset($intent_deleted)?>;
    var ai_state = <?php echo json_encode($bot['ai_status'])?>;
    var trainingFile = <?php echo($bot['training_file_uploaded'] ? 'true' : 'false'); unset($bot)?>;

    function searchIntents(str) {
        showIntents(str);
    }

    $(document).ready(function () {
        showIntents('');
    });
</script>
</body>
</html>