<div class="box-body bot-card unselectable" id="botcard">
        <div class="col-xs-4 no-padding bg-blue-gradient">
            <div class="bot-image ">
                <div class="bot-icon" id="botIcon">
                    <i class="fa fa-question" style="padding-top:45px;"></i>
                </div>
            </div>
        </div>
    
        <div class="col-xs-8 bot-info">

                <div class="row no-margin">
                    <!--title-->
                    <div class="col-xs-6 bot-title text-white">
                        <span id="botTitle"></span>
                    </div>
                    <!--badge-->
                    <div class="col-xs-6 bot-badge">
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
                    <!--rating-->
                    <div class="col-xs-5 bot-star">
                        <span id="botUsers"></span> users
                        <div class="star-rating text-right">
                            <div class="star-rating__wrap">
                              <span id="botRating"></span>
                            </div>
                        </div>
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
                        <!--<a href="" id="btnBuyBotBack" class="btn btn-primary text-center flat" style="width:125px;"></a>-->
                        <button class="btn btn-success pull-right flat" id="btnBuyBot" data-toggle="modal" data-target="#buyBot" style="width:135px;">
                            <b>Buy Bot </b>
                            <span class="fa fa-arrow-circle-right"></span>
                        </button>
                    </div>
                </div>
            
        </div>
</div>

<script>
    var bot = <?php
        $bot = new \hutoma\bot();
        if (isset($botDetails) && (array_key_exists('bot', $botDetails))) {

            $bot->setAiid($botDetails['bot']['aiid']);
            $bot->setAlertMessage($botDetails['bot']['alertMessage']);
            $bot->setBadge('Top Developer');                        //$botDetails['bot']['badge']);
            $bot->setBotId($botDetails['bot']['botId']);
            $bot->setCategory($botDetails['bot']['category']);
            $bot->setClassification($botDetails['bot']['classification']);
            $bot->setDescription($botDetails['bot']['description']);
            $bot->setLicenseType($botDetails['bot']['licenseType']);
            $bot->setUpdate($botDetails['bot']['lastUpdate']);
            $bot->setLongDescription($botDetails['bot']['longDescription']);
            $bot->setName($botDetails['bot']['name']);
            $bot->setPrice($botDetails['bot']['price']);
            $bot->setPrivacyPolicy($botDetails['bot']['privacyPolicy']);
            $bot->setSample($botDetails['bot']['sample']);
            $bot->setUsers('103');                                  //$botDetails['bot']['users']);
            $bot->setRating('4.3');                                   //$botDetails['bot']['rating']);
            $bot->setActivations($bot->rangeActivation($bot->getUsers()));
            $bot->setVersion($botDetails['bot']['version']);
            $bot->setVideoLink($botDetails['bot']['videoLink']);
        }
        $tmp_bot = $bot->toJSON();
        unset($bot);

        echo json_encode($tmp_bot);
        unset($tmp_bot);
        ?>;
</script>
