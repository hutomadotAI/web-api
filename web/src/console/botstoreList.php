<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

$header_page_title = "Botstore";
include __DIR__ . "/include/page_head_default.php";
?>

<body class="hold-transition skin-blue fixed " onload="scrollTo(0,0)">

<!-- ================ PAGE CONTENT ================= -->
<section class="content">
    <div class="overlay carousel-ovelay" id="carousel-overlay">
        <i class="fa fa-refresh fa-spin center-block"></i>
    </div>
    <p id="botsCarousels"></p>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/smoothness/jquery-ui.css">
    <div class="modal fade" id="buyBot" role="dialog">
        <div class="modal-dialog flat width600" id="modalDialog">

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
                                        <button type="button" class="close text-white" id="btnModelClose" data-dismiss="modal">&times;</button>
                                    </div>
                                </div>

                                <div class="row no-margin">
                                    <!--description-->
                                    <div class="col-xs-12" style="padding:2px 15px 0 0;">
                                        <textarea class="bot-default-style bot-description-limited flat no-shadow unselectable" id="botDescriptionPurchase" readonly></textarea>
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
                                        <span id="botPricePurchase"></span><span class="bot-badge no-padding text-orange"> &#8364</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-4 bot-buy-alert">
                                <span id="message"></span>
                            </div>
                            <div class="col-xs-4 bot-buy-purchase">
                                <!--purchased button-->
                                <button class="btn btn-success pull-right flat" id="btnPayment" data-dismiss="modal" data-flow="" style="width:130px;">
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
</section>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>
<script src="<? $assets->getAsset('botstore/botstore.js') ?>"></script>
<script src="<? $assets->getAsset('botstore/carousel.js') ?>"></script>
<script src="<? $assets->getAsset('botcard/botcard.js') ?>"></script>
<script src="<? $assets->getAsset('botcard/buyBot.js') ?>"></script>
<script src="/console/dist/vendors/mustache.min.js"></script>
<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>

<?php
$category = isset($_GET['category']) ? $_GET['category'] : "";
$showHeader = isset($_GET['showHeader']) ? $_GET['showHeader'] : 'true';
$openFullStore = isset($_GET['openFullStore']) ? $_GET['openFullStore'] : 'false';
?>
<script>
    $(document).ready(function () {
        getCarousels(
            '<?php echo $category ?>',
            <?php echo $showHeader?>,
            <?php echo $openFullStore?>);
    });
    $('#buyBot').on('hide.bs.modal', function (e) {
        var purchase_state = document.getElementById('purchase_state').value;
        if (purchase_state === 'true') {
            switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.BOTSTORE_FLOW.value);
        }
    });
    <?php
    unset($aiName);
    unset($category);
    unset($isExistAiId);
    ?>
</script>
</body>
</html>