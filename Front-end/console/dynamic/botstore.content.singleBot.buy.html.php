<div class="modal fade" id="buyBot" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content no-border bot-trasparent" style="background-color: transparent;" >

            <div class="modal-body no-padding no-shadow no-border" >
                <div class="box-body" style="min-width: 500px;">

                    <div class="row no-margin">
                            <div class="col-xs-1 no-padding">
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-trasparent bot-20">
                                    </div>
                                </div>
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-130" style="background:#212121;">
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-3 no-padding">
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-150 <?php echo $bot['widgetColor'];?> text-bg">
                                            <i class="<?php echo $bot['iconPath'];?>"></i>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-8 no-padding">
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-trasparent bot-20">
                                    </div>
                                </div>
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-50" style="background:#212121;">
                                        <div class="col-xs-8 bot-buy-title">
                                            <?php echo $bot['name'];?>
                                        </div>
                                        <div class="col-xs-4 bot-buy-price" id="botTitle">   <!--title-->

                                            <div class="pull-right text-orange text-md"> price
                                                <?php echo number_format($bot['licenceFee'], 2, '.', ''); ?> £
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row no-margin">
                                    <div class="col-xs-12 bot-80" style="background:#212121;padding-top:10px;">
                                        <div class="text-md"><i class="fa fa-credit-card "></i> Add credit o debit card</div>
                                        <div class="text-md"><i class="fa fa-cc-paypal"></i> Add PayPal</div>
                                        <div class="text-md"><i class="fa  fa-gift"></i> Add promotional code</div>
                                    </div>
                                </div>
                            </div>
                    </div>

                    <div class="row no-margin" style="min-height:50px;background:#212121;">
                    </div>

                </div>
            </div>

        </div>

    </div>
</div>