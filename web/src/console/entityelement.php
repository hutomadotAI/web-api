<?php

namespace hutoma;

require_once __DIR__ . "/common/errorRedirect.php";
require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/entityApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";

$assets = new Assets();

sessionObject::redirectToLoginIfUnauthenticated();


if (!isPostInputAvailable()) {
    errorRedirect::defaultErrorRedirect();
    exit;
}

$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

if (isset($_POST['entity_name'])) {
    $entityName = $_POST['entity_name'];
    $retValue = $entityApi->updateEntity($entityName, $_POST['entity_values']);

    if (isset($retvalue) && $retvalue['status']['code'] != 200) {
        errorRedirect::handleErrorRedirect($retvalue);
        exit;
    }
} else {
    $entityName = $_POST['entity'];
}

$entity_values_list = $entityApi->getEntityValues($entityName);
unset($entityApi);

if ($entity_values_list['status']['code'] !== 200 && $entity_values_list['status']['code'] !== 404) {
    $entity_result = $entity_values_list;
    unset($entity_values_list);
    errorRedirect::handleErrorRedirect($entity_result);
    exit;
}

function isPostInputAvailable()
{
    return (isset($_POST['entity']) || isset($_POST['entity_name']));
}

$header_page_title = "Edit Entity";
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
                    <input type="hidden" name="entity-name" id="entity-name" value="<?= $_POST['entity'] ?>">
                    <div class="box box-solid box-clean flat no-shadow unselectable">

                        <div class="box-header no-border" style="padding: 10px 10px 5px 10px;">
                            <div class="form-group no-margin">
                                <div class="input-group">
                                    <div class="input-prefix-text">
                                        <i class="fa fa-sitemap text-yellow"></i>
                                        <span><b> Entity </b></span><span class="text-md text-darkgray" style="padding-right:3px;">:</span>
                                    </div>
                                    <input type="text" class="flat no-shadow input-text-limited pull-left" value="@<?= $_POST['entity'] ?>" readonly>
                                    <button class="input-postfix-button btn btn-success flat pull-right" id="btnSaveEntity" style="width: 130px; "
                                            alt="save entity" onclick="saveEntity();RecursiveUnbind($('#wrapper'))">Save Entity
                                    </button>
                                </div>
                            </div>
                        </div>

                    </div>
                    <div class="box box-solid box-clean flat no-shadow unselectable">
                        <div class="box-header with-border ">
                            <i class="fa fa-sitemap text-yellow"></i>
                            <div class="box-title">
                                <b>Values</b>
                            </div>
                            <a data-toggle="collapse" href="#collapseValuesInfo">
                                <div class="pull-right">more info
                                    <i class="fa fa-question-circle text-sm text-yellow"></i>
                                </div>
                            </a>
                        </div>

                        <div id="collapseValuesInfo" class="panel-collapse collapse">
                            <div class="box-body" style="padding-bottom:0;">
                                <div class="overlay center-block">
                                    <section class="content-info">
                                        <div class="box-body">
                                            <dd>
                                                Entities are the sets of strings you want your bot to recognise and extract from a conversation.
                                            </dd>
                                        </div>
                                    </section>
                                </div>
                            </div>
                        </div>

                        <div class="box-body no-margin" id="boxValues" style="padding-top:0;">

                            <div class="row">
                                <div class="col-md-12">
                                    <h5 class="box-title">
                                        <div class="input-group no-margin">
                                            <input type="text" class="form-control flat no-shadow" id="value-entity" name="value-entity"
                                                   placeholder="Add value..." onkeyup="checkValueCode(this,event.keyCode)"
                                                   style="width: 96%;">
                                            <span class="input-group-btn">
                            <button class="btn btn-success flat" id="btnAddEntityValue" style="width: 130px;">Add Entity Value</button>
                        </span>
                                        </div>
                                    </h5>
                                </div>
                            </div>

                            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntityValues" style="margin-bottom:10px;">
                                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                                <i class="icon fa fa-check" id="iconAlertEntityValues"></i>
                                <span id="msgAlertEntityValues">You can add additional values to the current entity.</span>
                            </div>

                            <div class="row" id="entityValues-list"></div>
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
<script src="/console/dist/vendors/saveFile/FileSaver.js"></script>
<script src="<? $assets->getAsset('validation/validation.js') ?>"></script>
<script src="<? $assets->getAsset('entity/entity.element.js') ?>"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>

<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "entities", 1, false, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>
    var entityValuesListFromServer = <?php echo json_encode($entity_values_list['entity_values']);  unset($entity_values_list);;?>;
</script>
</body>
</html>
