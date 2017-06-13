<?php
require_once "api/apiBase.php";
require_once "api/developerApi.php";
require_once "api/botstoreApi.php";
require_once "common/botstoreItem.php";
require_once "common/sessionObject.php";

header('P3P: CP="CAO PSA OUR"');
session_start();

$botId = $_GET['botId'];
if (isset($_GET['origin'])) {
    $menu_title = $_GET['origin'];
} else {
    if (\hutoma\sessionObject::getDevToken() != null) {
        $menu_title = "botstore";
    }
}
$session = new hutoma\sessionObject();
$botstoreApi = new \hutoma\api\botstoreApi(false, $session->getDevToken());
$botstoreItem = $botstoreApi->getBotstoreBot($botId);

unset($botstoreApi);
unset($session);
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <?php
        if (isset($botstoreItem)) {
            $metadata = $botstoreItem['item']['metadata'];
            $developer = $botstoreItem['item']['developer'];

            $wHtml = '<title>hu:toma | Botstore | ' . $metadata['name'] . '</title>';
            $wHtml .= '<meta name="description" content="Hutoma Botstore,';
            $wHtml .= 'Bot name=' . $metadata['name'] . ',';
            $wHtml .= 'Category=' . $metadata['category'] . ',';
            $wHtml .= 'Description=' . $metadata['description'] . ',';
            $wHtml .= 'Developer=' . $developer['company'] . '">';
            echo $wHtml;
            unset ($metadata);
            unset ($developer);
            unset ($wHtml);
        }else{
            echo '<title>hu:toma | Botstore </title><meta name="description" content="Hutoma Botstore"';
        }
    ?>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <?php include_once "./dynamic/hotjar.inc.php" ?>
</head>
<body class="hold-transition fixed">

<!-- ================ ALERT MESSAGE BOX ================= -->
<div class="row center-block bot-card-alert" id="containerMsgAlertBotcardDetail" style="display:none;">
    <div class="alert alert-dismissable flat alert-danger no-margin">
        <button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>
        <i class="icon fa fa-warning" id="iconAlertBotcardDetail"></i>
        <span id="msgAlertBotcardDetail"></span>
    </div>
</div>

<!-- ================ PAGE CONTENT ================= -->
<div class="col-md-12 no-margin no-padding" id="botcardDetailContent" style="display:none;">
    <div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">

        <!-- ================ BOT DETAILS TOP CARD INFO ================= -->
        <div class="bot-card">
            <div class="bot-card-icon left">
                <img class="bot-card-icon" id="botIcon">
            </div>

            <div class="bot-card-area">

                <div class="row no-margin">
                    <!--title-->
                    <div class="col-xs-8 bot-title">
                        <span id="botTitle"></span>
                    </div>
                    <!--badge-->
                    <div class="col-xs-3 bot-badge no-padding" id="botBadge">
                    </div>
                    <!--close button-->
                    <div class="col-xs-1 bot-close-button no-padding">
                        <a href="" id="btnBuyBotBack" class="fa fa-close text-md text-darkgray" target="_top"></a>
                    </div>
                </div>

                <div class="row no-margin">
                    <!--description-->
                    <div class="col-xs-9 bot-description">
                        <textarea
                                class="bot-default-style bot-description-limited pull-left flat no-shadow unselectable"
                                id="botDescription" readonly></textarea>
                    </div>

                    <!--star ratings-->
                    <div class="col-xs-3 bot-star">
                    </div>
                </div>

                <div class="row no-margin">
                    <!--message-->
                    <div class="col-xs-12 bot-msg">
                        <i class="fa fa-info-circle text-sm text-yellow" id="botMessageIcon"></i>
                        <input class="bot-default-style" id="botMessage" style="width:96%">
                    </div>
                </div>

                <div class="row no-margin">
                    <!--other-->
                    <div class="col-xs-12 bot-other">
                    </div>
                </div>

                <div class="row no-margin">
                    <div class="col-xs-8 no-padding">
                        <!--licence-->
                        <div class="row no-margin bot-licence">
                            licence <span id="botLicense"></span>
                        </div>
                        <!--price-->
                        <div class="row no-margin bot-price">
                            <div class="pull-left text-orange">
                                price
                                <span class="text-orange"></span>
                                <span id="botPrice"></span><span class="bot-badge no-padding text-orange"> &#8364</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-4 bot-button">
                        <button class="btn btn-success pull-right flat" id="btnBuyBot" style="width:135px;">
                            <b>Use Bot </b>
                            <span class="fa fa-arrow-circle-right"></span>
                        </button>
                    </div>
                </div>

            </div>
        </div>

        <!-- ================ BOT DETAILS ALERT BOX ============================= -->
        <div class="bot-alert-msg" id="msgAlertBotcardBox">
            <div id="containerMsgAlertBotcard">
                <i id="iconAlertBotcard"></i>
                <span id="msgAlertBotcard"></span>
            </div>
        </div>

        <!-- ================ BOT DETAILS VIDEO ============================= -->
        <span id="botVideoLinkSection">
            <div class="box-body flat unselectable">
                <div class="col-xs-12 no-padding bot-video flat">
                    <div class="box-body no-padding flat">
                        <div class="overlay center-block" style="background-color: black;">
                            <div class="embed-responsive embed-responsive-16by9">
                                <embed id="botVideoLink" src="" frameborder="0" allowfullscreen></embed>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </span>
        <!-- ================ BOT DETAILS DESCRIPTION INFO ================= -->
        <div class="box-body unselectable">
            <div class="row no-margin">
                <div class="col-xs-12 bot-h2">
                    Description
                </div>
            </div>
            <div class="row no-margin" style="padding-bottom:10px;">
                <div class="col-xs-12 bot-detail">
                    <span id="botLongDescription"></span>
                </div>
            </div>
            <div class="row no-margin" style="padding-top:10px;">
                <div class="col-xs-12 bot-h2">
                    Sample Request
                </div>
            </div>
            <div class="row no-margin" style="padding-bottom:10px;">
                <div class="col-xs-12 text-gray bot-detail">
                    <div class="box-body flat" style="background: #343434; border: 1px solid #737373;">
                        <div class="text-gray"><span id="botSample"></span></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ================ BOT DETAILS FOOTER INFO ======================= -->
        <div class="box-footer">
            <div class="row no-margin unselectable">
                <div class="col-xs-12 bot-h2">
                    More details
                </div>
            </div>
            <div class="row no-margin unselectable">
                <div class="col-xs-4">
                    <div class="bot-more-details">Last Update</div>
                    <div class="text-left">
                        <span id="botLastUpdate"></span>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="bot-more-details">Category</div>
                    <div class="text-left">
                        <span id="botCategory"></span>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="bot-more-details">Version</div>
                    <div class="text-left">
                        <span id="botVersion"></span>
                    </div>
                </div>
            </div>

            <div class="row no-margin unselectable">
                <div class="col-xs-4">
                    <div class="bot-more-details">Offer by</div>
                    <div class="text-left">
                        <span id="botCompany"></span>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="bot-more-details">Classification</div>
                    <div class="text-left">
                        <span id="botClassification"></span>
                    </div>
                </div>
                <div class="col-xs-4 unselectable" id="botPrivacyPolicySection">
                    <div class="bot-more-details">Privacy</div>
                    <div class="text-left">
                        <a class="dev-link" id="botPrivacyPolicy" href="" rel="nofollow" target="_blank">View Privacy
                            Policy</a>
                    </div>
                </div>
                <div class="col-xs-4" id="developerInfo">
                    <div class="bot-more-details unselectable">Developer</div>
                    <div class="text-left">
                        <a class="dev-link unselectable" id="botWebsite" href="" rel="nofollow" target="_blank">Visit
                            Website</a>
                    </div>
                </div>
            </div>

            <br>
            <a href="./botstore.php" id="btnBackToBotstore" class="btn btn-primary pull-left flat" target="_top">Go to
                Botstore</a>
        </div>
    </div>


    <!-- ================ BOT MODAL POPUP BOT PURCHASE ================= -->

    <div class="modal fade" id="buyBot" role="dialog" style="padding-top:100px;">
        <div class="modal-dialog flat">

            <div class="modal-content bot-shadow">

                <div class="modal-body no-padding no-shadow no-border">
                    <div class="box-body bot-payment flat">

                        <div class="row no-margin">
                            <div class="col-xs-4 no-padding">
                                <div class="bot-icon-payment bot-absolute bot-shadow-light text-bg">
                                    <img class="card-icon" style="margin-top: 0" src="" id="botIconPurchase">
                                </div>
                            </div>
                            <div class="col-xs-8 no-padding">
                                <div class="row no-margin">
                                    <!--title-->
                                    <div class="col-xs-11 bot-buy-title text-white">
                                        <span id="botNamePurchase"></span>
                                    </div>
                                    <!--close button-->
                                    <div class="col-xs-1 bot-close-button">
                                        <button type="button" class="close text-white" id="btnModelClose"
                                                data-dismiss="modal">&times;
                                        </button>
                                    </div>
                                </div>

                                <div class="row no-margin">
                                    <!--description-->
                                    <div class="col-xs-12" style="padding:2px 15px 0 0;">
                                        <textarea
                                                class="bot-default-style bot-description-limited flat no-shadow unselectable"
                                                id="botDescriptionPurchase" readonly></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row no-margin">
                            <div class="col-xs-4 bot-buy-details">
                                <!--licence-->
                                <div class="row no-margin bot-licence-purchase" style="visibility:hidden;">
                                    licence <span id="botLicensePurchase"></span>
                                </div>
                                <!--price-->
                                <div class="row no-margin bot-price-purchase">
                                    <div class="pull-left text-orange">
                                        price
                                        <span class="text-orange"></span>
                                        <span id="botPricePurchase"></span><span
                                                class="bot-badge no-padding text-orange"> &#8364</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-4 bot-buy-alert">
                                <span id="message"></span>
                            </div>
                            <div class="col-xs-4 bot-buy-purchase">
                                <!--purchased button-->
                                <button class="btn btn-success pull-right flat" id="btnPayment" data-dismiss="modal"
                                        data-flow="" style="width:130px;">
                                    <b>Use Bot </b>
                                    <span class="fa fa-arrow-circle-right"></span>
                                </button>
                            </div>
                        </div>

                    </div>
                </div>

            </div>

        </div>
    </div>
    <input type="hidden" id="purchase_state" name="purchase_state" value="false" style="display:none;">
    <input type="hidden" id="bot_id" name="bot_id" style="display:none;">

</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/botcard/botcard.js"></script>
<script src="./scripts/botstore/botstoreWizard.js"></script>
<script src="./scripts/botcard/buyBotFromBotcardDetail.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
if (isset($botstoreItem)) {
    ?>
    <script>
        var responseCode = <?php echo $botstoreItem['status']['code']; ?>;
        var botstoreItem = <?php
            $botItem = new \hutoma\botstoreItem();
            if (isset($botstoreItem) && (array_key_exists('item', $botstoreItem)))
                $botItem = \hutoma\botstoreItem::fromObject($botstoreItem['item']);
            echo json_encode($botItem->toJSON());
            unset($botItem);
            ?>;
    </script>
<?php } ?>
<script>
    $(function () {
        var nodeContainerAlert = document.getElementById('containerMsgAlertBotcardDetail');
        var nodeMessageAlert = document.getElementById('msgAlertBotcardDetail');

        <?php if ( isset($botId,$botstoreItem) ) { unset($botstoreItem);?>
        switch (responseCode) {
            case 200:
                populateBotFields(
                    botstoreItem,
                    "<?php echo(isset($menu_title) ? $menu_title : ""); unset($menu_title)?>",
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
