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
                <a href="" id="btnBuyBotBack" class="fa fa-close text-md text-darkgray"></a>
            </div>
        </div>

        <div class="row no-margin">
            <!--description-->
            <div class="col-xs-9 bot-description">
                <textarea class="bot-default-style bot-description-limited pull-left flat no-shadow unselectable" id="botDescription" readonly></textarea>
            </div>

            <!--tmp div to fix UI to be updated once we have star ratings-->
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
                <button class="btn btn-success pull-right flat" id="btnBuyBot" data-toggle="modal" data-target="#buyBot" style="width:135px;">
                    <b>Use Bot </b>
                    <span class="fa fa-arrow-circle-right"></span>
                </button>
            </div>
        </div>

    </div>
</div>