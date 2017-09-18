<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/common/botstoreItem.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/developerApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";

$assets = new Assets();

//header('P3P: CP="CAO PSA OUR"');

$botId = $_GET['botId'];
if (isset($_GET['origin'])) {
    $menu_title = $_GET['origin'];
} else {
    if (sessionObject::getDevToken() != null) {
        $menu_title = "botstore";
    }
}

$botstoreApi = new api\botstoreApi(false, sessionObject::getDevToken());
$botstoreItem = $botstoreApi->getBotstoreBot($botId);
unset($botstoreApi);

$metadata = $botstoreItem['item']['metadata'];
$developer = $botstoreItem['item']['developer'];


$botItem = new botstoreItem();
if (isset($botstoreItem) && (array_key_exists('item', $botstoreItem))) {
    $botItem = botstoreItem::fromObject($botstoreItem['item']);
}
$botItemJson = json_encode($botItem->toJSON());
unset($botItem);


$metaEntry = '<meta name="description" content="Hutoma Botstore,';
$metaEntry .= 'Bot name=' . $metadata['name'] . ',';
$metaEntry .= 'Category=' . $metadata['category'] . ',';
$metaEntry .= 'Description=' . $metadata['description'] . ',';
$metaEntry .= 'Developer=' . $developer['company'] . '">';

$header_page_title = "Botstore | " . $metadata['name'];
$header_additional_entries = $metaEntry;
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<body class="hold-transition fixed" onload="scrollTo(0,0)">

<!-- ================ ALERT MESSAGE BOX ================= -->
<div class="row center-block bot-card-alert" id="containerMsgAlertBotcardDetail" style="display:none;">
    <div class="alert alert-dismissable flat alert-danger no-margin">
        <button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>
        <i class="icon fa fa-warning" id="iconAlertBotcardDetail"></i>
        <span id="msgAlertBotcardDetail"></span>
    </div>
</div>

<!-- ================ PAGE CONTENT ================= -->
<div id="botcardDetailContent"></div>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>
<script src="/console/dist/vendors/mustache.min.js"></script>
<script src="<? $assets->getAsset('botcard/botcard.js') ?>"></script>
<script src="<? $assets->getAsset('botcard/buyBotFromBotcardDetail.js') ?>"></script>

<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
if (isset($botstoreItem)) {
    ?>
    <script>
        var responseCode = <?php echo $botstoreItem['status']['code']; ?>;
        var botstoreItem = <?php echo $botItemJson ?>;
    </script>
<?php } ?>
<script>
    $(function () {
        var nodeContainerAlert = document.getElementById('containerMsgAlertBotcardDetail');
        var nodeMessageAlert = document.getElementById('msgAlertBotcardDetail');

        <?php if ( isset($botId, $botstoreItem) ) { unset($botstoreItem);?>
        switch (responseCode) {
            case 200:
                displayBotCardDetail(botstoreItem,
                    <?php
                    if (!isset($menu_title)) {
                        $menu_title = "other";
                    }
                    switch ($menu_title) {
                        case "botstore":
                            echo "BOTCARD_DETAIL.BOTSTORE";
                            break;
                        case "settings":
                            echo "BOTCARD_DETAIL.SETTINGS";
                            break;
                        default:
                            echo "BOTCARD_DETAIL.OTHER";
                            break;
                    }
                    unset($menu_title);
                    ?>,
                    "<?php if (isset($_GET['category'])) echo $_GET['category'];?>",
                    DRAW_BOTCARDS.BOTSTORE_WITH_BOT_FLOW.value
                );
                break;
            case 404:
                nodeContainerAlert.style.display = 'block';
                nodeMessageAlert.innerText = 'Bot not found';
                break;
            case 500:
                nodeContainerAlert.style.display = 'block';
                nodeMessageAlert.innerText = 'There was a problem acquiring the bot';
                break;
            default:
                nodeContainerAlert.style.display = 'block';
                nodeMessageAlert.innerText = 'Something has gone wrong.';
        }
        <?php } else { ?>
        nodeContainerAlert.style.display = 'block';
        nodeMessageAlert.innerText = 'Bot not found';
        <?php } ?>
    });
</script>


</body>
</html>
