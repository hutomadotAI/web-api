<div class="box-body bot-card flat unselectable" id="botcard">
        <div class="col-xs-4 no-padding bg-blue-gradient">
            <img class="bot-icon">
        </div>
    
        <div class="col-xs-8 bot-info">

                <div class="row no-margin">
                    <!--title-->
                    <div class="col-xs-10 bot-title text-white">
                        <span id="botTitle"></span>
                    </div>
                    <!--badge-->
                    <div class="col-xs-2 bot-badge">
                        <span id="botBadge" hidden></span>
                        <!--<i class="fa fa-ra text-aqua"></i>-->
                        <a href="" id="btnBuyBotBack" class="fa fa-close text-md text-darkgray"></a>
                    </div>
                </div>

                <div class="row no-margin">
                    <!--description-->
                    <div class="col-xs-7 bot-description">
                        <span id="botDescription"></span>
                    </div>

                    <!--tmp div to fix UI to be updated once we have star ratings-->
                    <div class="col-xs-5 bot-star">
                    </div>
                </div>

                <div class="row no-margin">
                    <!--message-->
                    <div class="col-xs-12 bot-msg">
                        <i class="fa fa-info-circle text-sm text-yellow"></i>
                        <span id="botMessage"></span>
                    </div>
                </div>

                <div class="row no-margin">
                    <!--other-->
                    <div class="col-xs-12 bot-other">
                    </div>
                </div>

                <div class="row no-margin">
                    <div class="col-xs-4 no-padding">
                        <!--licence-->
                        <div class="row no-margin bot-licence">
                            licence <span id="botLicense"></span>
                        </div >
                        <!--price-->
                        <div class="row no-margin bot-price">
                            <div class="pull-left text-orange">price <span class="text-orange"></span> <span id="botPrice"></span><span class="bot-badge no-padding text-orange"> &#8364</span></div>
                        </div >
                    </div>
                    <div class="col-xs-8 bot-buy">
                        <button class="btn btn-success pull-right flat" id="btnBuyBot" data-toggle="modal" data-target="#buyBot" style="width:135px;">
                            <b>Buy Bot </b>
                            <span class="fa fa-arrow-circle-right"></span>
                        </button>
                    </div>
                </div>
            
        </div>
</div>