<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/entityApi.php";
require_once __DIR__ . "/api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();


$entityApi = new api\entityApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$entities = $entityApi->getEntities();
unset($entityApi);

if ($entities['status']['code'] !== 200 && $entities['status']['code'] !== 404) {
    unset($entities);
    utils::redirect('./error.php?err=225');
    exit;
}

function echoJsonEntitiesResponse($entities)
{
    if ($entities['status']['code'] !== 404) {
        echo json_encode($entities['entities']);
    }
    else
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
                    <?php include __DIR__ . '/dynamic/entity.content.create.html.php'; ?>
                    <?php include __DIR__ . '/dynamic/entity.content.list.html.php'; ?>
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

<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/entity/entity.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "entities", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>


<script>
    var entities = <?php echoJsonEntitiesResponse($entities); unset($entities)?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'entities_list';

    function searchEntities(str) {
        showEntities(str);
    }

    $(document).ready(function () {
        showEntities('');
    });
</script>
</body>
</html>
