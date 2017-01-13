<link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/smoothness/jquery-ui.css">
<div class="modal fade" id="buyBot" role="dialog" style="padding-top:100px;">
    <div class="modal-dialog flat">

        <div class="modal-content bot-shadow" >

            <div class="modal-body no-padding no-shadow no-border" >
                <div class="box-body bot-payment flat">
                    <div class="row no-margin">
                        <div class="col-xs-4 no-padding">
                            <div class="bot-icon-payment bot-absolute bot-shadow-light bg-blue-gradient text-bg">
                                <div class=".bot-icon-payment">
                                    <i class="fa fa-question text-white"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-8 no-padding">
                            <button type="button" class="close text-white" id="btnModelClose" data-dismiss="modal">&times;</button>
                            <div class="row no-margin">
                                <div class="col-xs-12 bot-30">
                                    <div class="col-xs-8 bot-buy-title">
                                        <span id="botNamePurchase"></span>
                                    </div>
                                    <div class="col-xs-4 bot-buy-price" id="botTitle">
                                        <div class="pull-right text-orange text-md"> price
                                            <span id="botPricePurchase"></span> &#8364
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row no-margin">
                                <div class="col-xs-6 bot-70" style="padding-top:20px;">
                                    <span class="text-gray" id="botDescriptionPurchase"></span>
                                </div>
                                <div class="col-xs-6 bot-70" style="padding-top:35px;">
                                    <button class="btn btn-success pull-right flat" data-dismiss="modal" id="btnPayment" style="width:130px;">
                                        <b>Buy Bot </b>
                                        <span class="fa fa-arrow-circle-right"></span>
                                    </button>
                                </div>
                            </div>
                            <div class="row no-margin">
                                <span id="message"></span>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
<input type="hidden" id="purchase_state" name="purchase_state" value="0">
<input type="hidden" id="bot_id" name="bot_id">