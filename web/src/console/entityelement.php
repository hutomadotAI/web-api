<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/entityApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();


if (!isPostInputAvailable()) {
    utils::redirect('./error.php?err=119');
    exit;
}

$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

if (isset($_POST['entity_name'])) {
    $entityName = $_POST['entity_name'];
    $retValue = $entityApi->updateEntity($entityName, $_POST['entity_values']);

    if (isset($retvalue) && $retvalue['status']['code'] != 200) {
        utils::redirect('./error.php?errObj=' . urlencode($retvalue), null);
        exit;
    }
} else {
    $entityName = $_POST['entity'];
}

$entity_values_list = $entityApi->getEntityValues($entityName);
unset($entityApi);

if ($entity_values_list['status']['code'] !== 200) {
    unset($entity_values_list);
    utils::redirect('./error.php?err=225');
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

    <div class="content-wrapper" style="margin-right:350px;">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <?php include __DIR__ . '/dynamic/entity.element.content.head.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/entity.element.content.values.html.php'; ?>
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

<script src="scripts/external/saveFile/FileSaver.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/entity/entity.element.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "entities", 1, false, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>
    var entityValuesListFromServer = <?php echo json_encode($entity_values_list['entity_values']);  unset($entity_values_list);;?>;
</script>
</body>
</html>
