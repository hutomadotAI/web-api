<div class="modal fade" id="buyBot" role="dialog" style="padding-top:100px;">
    <div class="modal-dialog flat">

        <div class="modal-content bot-shadow" >

            <div class="modal-body no-padding no-shadow no-border" >
                <div class="box-body bot-payment flat">
                    <div class="row no-margin">
                        <div class="col-xs-4 no-padding">
                            <div class="bot-icon-payment bot-absolute bot-shadow-light <?php echo $bot['widgetColor'];?> text-bg">
                                <div class=".bot-icon-payment">
                                    <i class="<?php echo $bot['iconPath'];?> text-white"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-8 no-padding">

                            <div class="row no-margin">
                                <div class="col-xs-12 bot-50">
                                    <div class="col-xs-8 bot-buy-title">
                                        <?php echo $bot['name'];?>
                                    </div>
                                    <div class="col-xs-4 bot-buy-price" id="botTitle">
                                        <div class="pull-right text-orange text-md"> price
                                            <?php echo number_format($bot['licenceFee'], 2, '.', ''); ?> £
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row no-margin">
                                <div class="col-xs-6 bot-70" style="padding-top:20px;">
                                    <div class="text-sm"><i class="fa fa-credit-card "></i> Add credit o debit card</div>
                                    <div class="text-sm"><i class="fa fa-cc-paypal"></i> Add PayPal</div>
                                    <div class="text-sm"><i class="fa  fa-gift"></i> Add promotional code</div>
                                </div>
                                <div class="col-xs-6 bot-70" style="padding-top:35px;">
                                    <button class="btn btn-success pull-right flat" id="btnBuyBot" data-toggle="modal" data-target="#buyBot"value="" alt="buy a bot">
                                        <b>Buy new Bot</b>
                                        <span class="fa fa-arrow-circle-right"></span>
                                    </button>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>