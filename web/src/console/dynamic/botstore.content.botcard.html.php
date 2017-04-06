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

    <!-- ================ BOT DETAILS VIDEO ============================= -->
    <span id="botVideoLinkSection">
        <div class="box-body flat unselectable" >
            <div class="col-xs-12 no-padding bot-video flat" >
                <div class="box-body no-padding flat" >
                    <div class="overlay center-block" style="background-color: black;">
                        <div class="embed-responsive embed-responsive-16by9">
                            <iframe id="botVideoLink"
                                    src=""
                                    frameborder="0" allowfullscreen>
                            </iframe>
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
            <div class="col-xs-4 unselectable">
                <div class="bot-more-details">Privacy</div>
                <div class="text-left">
                    <a class="dev-link" id="botPrivacyPolicy" href="" rel="nofollow" target="_blank">View Privacy Policy</a>
                </div>
            </div>
        </div>

        <div class="row no-margin">
            <div class="col-xs-4" id="developerInfo">
                <div class="bot-more-details unselectable">Developer</div>
                <div class="text-left">
                    <a class="dev-link unselectable" id="botWebsite" href="" rel="nofollow" target="_blank">Visit
                        Website</a>
                </div>
            </div>
            <div class="col-xs-4 unselectable">
            </div>
        </div>
        <br>
        <a href="./botstore.php" id="bthBackToBotstore" class="btn btn-primary pull-left flat">Go to Botstore</a>
    </div>

</div>