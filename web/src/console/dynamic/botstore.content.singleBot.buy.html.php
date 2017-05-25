<link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/smoothness/jquery-ui.css">
<div class="modal fade" id="buyBot" role="dialog">
    <div class="modal-dialog flat" id="modalDialog">

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