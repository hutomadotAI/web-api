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
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();


$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$entities = $entityApi->getEntities();
unset($entityApi);

if ($entities['status']['code'] !== 200 && $entities['status']['code'] !== 404) {
    $entity_result = $entities;
    unset($entities);
    errorRedirect::handleErrorRedirect($entity_result);
    exit;
}

function echoJsonEntitiesResponse($entities)
{
    if ($entities['status']['code'] !== 404) {
        echo json_encode($entities['entities']);
    } else
        echo '""'; // return empty string
}

$header_page_title = "Entities";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <div class="content-wrapper">
        <section class="content">
            <div class="tab-content" style="padding-bottom:0px;">
                <div class="tab-pane active" id="page_general">

                    <div class="box box-solid box-clean flat no-shadow unselectable">

                        <div class="box-header with-border">
                            <i class="fa fa-sitemap text-yellow"></i>
                            <div class="box-title"><b>New Entity</b></div>
                            <a data-toggle="collapse" href="#collapseEntitiesInfo">
                                <div class=" pull-right">more info
                                    <i class="fa fa-question-circle text-sm text-yellow"></i>
                                </div>
                            </a>
                        </div>

                        <div id="collapseEntitiesInfo" class="panel-collapse collapse">
                            <div class="box-body" style="padding-bottom:0px;">
                                <div class="overlay center-block">
                                    <section class="content-info">
                                        <div class="box-body">
                                            <dd>
                                                Entities are objects that might be required to fulfil an intent. Imagine
                                                you are creating a
                                                Bot that takes orders in a bar, a customer may ask "I would like to
                                                order ...."X".
                                                X here is an entity you would want the Bot to extract from a
                                                conversation. These could
                                                include "beer", "wine" or "cola" which fall into the drinks category.
                                                You could list further
                                                entities under food.
                                            </dd>
                                        </div>
                                    </section>
                                </div>
                            </div>
                        </div>

                        <div class="box-body" id="boxEntities">
                            <div class="bootstrap-filestyle input-group" id="GrpEntityButton">
                                <input type="text" class="form-control flat no-shadow" id="inputEntityName"
                                       name="entity"
                                       placeholder="Give the entity a name" style="width: 96%;"
                                       onkeyup="checkEntityCode(this,event.keyCode)">
                                <div class="input-group-btn" tabindex="0">
                                    <button id="btnCreateEntity" class="btn btn-success flat" style="width: 120px;">
                                        Create Entity
                                    </button>
                                </div>
                            </div>
                            <p></p>

                            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntity"
                                 style="margin-bottom:10px;">
                                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                                <i class="icon fa fa-check" id="icongAlertEntity"></i>
                                <span id="msgAlertEntity">In this section you can create different entities.</span>
                            </div>
                        </div>

                        <div class="box-footer"
                        <span>
            If you’re stuck check out our <a data-toggle="collapse" href="#collapseVideoTutorialEntity">entities tutorial</a> or email <a
                                    href='mailto:support@hutoma.ai?subject=Invite%20to%20slack%20channel'
                                    tabindex="-1">support@hutoma.ai</a> for an invite to our slack channel.
        </span>
                        <p></p>


                        <div id="collapseVideoTutorialEntity" class="panel-collapse collapse">
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


                    <div class="box box-solid box-clean flat no-shadow unselectable">

                        <div class="box-header with-border">
                            <i class="fa fa-sitemap text-yellow"></i>
                            <div class="box-title"><b>Entity List</b></div>
                            <a data-toggle="collapse" href="#collapseEntitiesListInfo">
                                <div class=" pull-right">more info
                                    <i class="fa fa-question-circle text-sm text-yellow"></i>
                                </div>
                            </a>
                        </div>

                        <div id="collapseEntitiesListInfo" class="panel-collapse collapse">
                            <div class="box-body" style="padding-bottom:0px;">
                                <div class="overlay center-block">
                                    <section class="content-info">
                                        <div class="box-body">
                                            All the entities available to your bots are listed here.
                                        </div>
                                    </section>
                                </div>
                            </div>
                        </div>

                        <div class="box-body">
                            <div class="input-group-btn">
                                <input class="form-control flat no-shadow pull-right"
                                       onkeyup="searchEntities(this.value)" value="" placeholder="Search...">
                            </div>

                            <p></p>

                            <div class="tab-pane" id="tab_entities">
                                <p id="entsearch"></p>
                            </div>
                            <p></p>
                        </div>

                    </div>


                    <!-- Modal DELETE entity-->
                    <div class="modal fade" id="deleteEntity" role="dialog">
                        <div class="modal-dialog flat width600">
                            <!-- Modal content-->
                            <div class="modal-content bordered" style="background-color: #202020">
                                <div class="modal-header">
                                    <button type="button" class="close text-gray" data-dismiss="modal">&times;</button>
                                    <h4 class="modal-title">DELETE Entity</h4>
                                </div>
                                <div class="modal-body" style="background-color: #212121">
                                    <div class="box-body" id="delete-entity-label">

                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel
                                    </button>
                                    <button type="button" class="btn btn-danger flat" id="modalDelete" value=""
                                            onClick="deleteEntity(this.value)" data-dismiss="modal">Delete
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
<script src="<?php $assets->getAsset('validation/validation.js') ?>"></script>
<script src="<?php $assets->getAsset('entity/entity.js') ?>"></script>

<script src="<?php $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<?php $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "entities", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


<script>
    var entities = <?php echoJsonEntitiesResponse($entities); unset($entities)?>;

    function searchEntities(str) {
        showEntities(str);
    }

    $(document).ready(function () {
        showEntities('');
    });
</script>
</body>
</html>
