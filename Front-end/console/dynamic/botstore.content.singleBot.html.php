<?php
$bot['badge']='Top Developer';
$bot['badgeIcon']='fa fa-rebel';
$bot['users']='103';
$bot['price']='0.0';
$bot['msg']='Questi contenuti non sono disponibili in Italiano. Leggi ulteriori informazioni sulle lingue supportate.';
$bot['sample']='User: I want to sleep.<p></p>Agent: Need a pick-me-up? I can find somewhere nearby to get some coffee.<p></p><p></p>User: You\'re so sweet.<p></p>Agent: I like you too. You\'re a lot of fun to talk to.';
$bot['lastUpdate']='10 september 2016';
$bot['classification']='entertainment';
$bot['version']='2.1';
$bot['developer']='hu:toma Ltd.';
$bot['company']='HUTOMA';
$bot['contact']='support@hutoma.com';
$bot['address']='Carrer del Consell de Cent, 341';
$bot['postcode']='08007';
$bot['city']='Barcelona';
$bot['nation']='Spain';
$bot['permissionLink']='./botstore.php';
$bot['privacyLink']='./botstore.php';
$bot['reportLink']='./botstore.php';

$bot['longDescription']='A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart. I am alone, 
and feel the charm of existence in this spot, which was created for the bliss of souls like mine.
I am so happy, my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents.
I should be incapable of drawing a single stroke at the present moment
and yet I feel that I never was a greater artist than now.';

function licenceTypeToString($x){
    switch ($x) {
        case 0:
            return 'Trial';
        case 1:
            return 'Subscription';
        case 2:
            return 'Perpetual';
    }
}

function rangeActivation($n){
     switch (true) {
         case ($n < 10):
             return '0-10';
         case ($n < 100):
             return '10-100';
         case ($n < 1000):
             return '100-1000';
         case ($n < 5000):
             return '1.000-5.000';
         case ($n < 10000):
             return '5.000-10.000';
         case ($n < 20000):
             return '10.000-20.000';
         case ($n < 50000):
             return '20.000-50.000';
         case ($n < 100000):
             return '50.000-100.000';
         case ($n < 500000):
             return '100.000-500.000';
         case ($n < 1000000):
             return '500.000-1.000.000';
         case ($n < 5000000):
             return '1.000.000-5.000.000';
         case ($n < 10000000):
             return '5.000.000-10.000.000';
     }
}
?>

<div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">
    <div class="box-body bot-card" id="botcard">

        <div class="col-xs-4 no-padding <?php echo $bot['widgetColor'];?> ">
            <div class="bot-icon" id="botIcon">
                <i class="<?php echo $bot['iconPath'];?>" style="padding-top:45px;"></i>
            </div>
        </div>

        <div class="col-xs-8 bot-info">
            <div class="row no-margin">
                <div class="col-xs-6 bot-title text-white" id="botTitle">   <!--title-->
                    <?php echo $bot['name'];?>
                </div>
                <div class="col-xs-6 bot-badge" id="botBagde">      <!--developer-->
                    <i class="<?php echo $bot['badgeIcon'];?> text-aqua"></i> <?php echo $bot['badge'];?>
                </div>
            </div>

            <div class="row no-margin">
                <div class="col-xs-7 bot-description" id="botDescription">  <!--description-->
                    <?php echo $bot['description'];?>
                </div>
                <div class="col-xs-5 bot-star"  id="botUsers">           <!--rating-->
                    <?php echo $bot['users'];?> users
                    <div class="star-rating text-right">
                        <div class="star-rating__wrap">
                            <?php
                                for ($i=5; $i>0; $i--) {
                                    if ($i==intval($bot['rating'])) {
                                        echo '<input class="star-rating__input" id="star--rating-' . $i . '" type="radio" name="rating" value="' . $i . '" checked="checked" disabled="disabled">';
                                        echo '<label class="star-rating__ico fa fa-star-o fa-lg" for="star--rating-' . $i . '" title="' . $i . ' out of ' . $i . ' stars"></label>';
                                    }
                                    else {
                                        echo '<input class="star-rating__input" id="star--rating-' . $i . '" type="radio" name="rating" value="' . $i . '" disabled="disabled">';
                                        echo '<label class="star-rating__ico fa fa-star-o fa-lg" for="star--rating-' . $i . '" title="' . $i . ' out of ' . $i . ' stars"></label>';
                                    }
                                }
                            ?>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row no-margin">
                <div class="col-xs-12 bot-msg" id="botTitle">
                    <i class="fa fa-info-circle text-sm text-yellow"></i> <?php echo $bot['msg']?>
                </div>
            </div>
            <div class="row no-margin">
                <div class="col-xs-12 bot-other">
                </div>
            </div>

            <div class="row no-margin">
                <div class="col-xs-6 no-padding" id="botTitle">   <!--title-->
                    <div class="row no-margin bot-licence">
                        licence <?php echo strtoupper(licenceTypeToString($bot['licenceType']));?>
                    </div >
                    <div class="row no-margin bot-price">
                        <div class="pull-left text-orange">price <span class="text-orange"><?php echo number_format($bot['licenceFee'], 2, '.', ''); ?></span> <span class="bot-badge no-padding text-orange">£</span></div>
                    </div >
                </div>
                <div class="col-xs-6 bot-buy">
                    <button class="btn btn-success pull-right flat" id="btnBuyBot"> <b>Buy new Bot</b> <span class="fa fa-arrow-circle-right"></span></button>
                </div>
            </div>
        </div>
    </div>

    <div class="box-body  flat">
        <div class="col-xs-12 no-padding bot-video flat" id="botVideo">
            <div class="box-body no-padding flat">
                <div class="overlay center-block">
                    <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                        <iframe
                            src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                            frameborder="0" allowfullscreen>
                        </iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="box-body">
        <div class="row no-margin">
            <div class="col-xs-12 bot-h2">
                Description
            </div>
        </div>
        <div class="row no-margin" style="padding-bottom:10px;">
            <div class="col-xs-12 bot-detail" id="botLongDescription">
                <?php echo $bot['longDescription'];?>
            </div>
        </div>
        <div class="row no-margin" style="padding-top:10px; border-top: 1px solid #535353;">
            <div class="col-xs-12 bot-h2">
                Sample Request
            </div>
        </div>
        <div class="row no-margin" style="padding-bottom:10px;">
            <div class="col-xs-12 text-gray bot-detail" id="botSampleRequest">
                <div class="box-body flat" style="background: #343434; border: 1px solid #737373;">
                    <div class="text-gray"><?php echo $bot['sample'];?></div>
                </div>
            </div>
        </div>
    </div>

    <div class="box-footer">
        <div class="row no-margin">
            <div class="col-xs-12 bot-h2">
                More details
            </div>
        </div>
        <div class="row no-margin">
            <div class="col-xs-4">
                <div class="bot-more-details">Last Update</div>
                <div class="text-left" id="botUpdated">
                    <?php echo $bot['lastUpdate'];?>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details" id="botClassification">Classification</div>
                <div class="text-left">
                    <?php echo $bot['classification'];?>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details">Version</div>
                <div class="text-left" id="botVersion">
                    <?php echo $bot['version'];?>
                </div>
            </div>
        </div>

        <div class="row no-margin">
            <div class="col-xs-4">
                <div class="bot-more-details">Offer by</div>
                <div class="text-left" id="botCompany">
                    <?php echo $bot['company'];?>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details">Permissions</div>
                <div class="text-left" id="botPermission">
                    <a class="dev-link" href="<?php echo $bot['permissionLink'];?>" rel="nofollow" target="_blank">View details</a>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details">Activations</div>
                <div class="text-left" id="botActivations">
                    <?php echo rangeActivation($bot['activations']);?>
                </div>
            </div>
        </div>

        <div class="row no-margin">
            <div class="col-xs-4">
                <div class="bot-more-details">Report</div>
                <div class="text-left" id="botReport">
                    <a class="dev-link" href="<?php echo $bot['reportLink'];?>" rel="nofollow" target="_blank">Report as inappropriate</a>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details">Privacy</div>
                <div class="text-left" id="botPrivacyPage">
                    <a class="dev-link" href="<?php echo $bot['privacyLink'];?>" rel="nofollow" target="_blank">View Privacy Policy</a>
                </div>
            </div>
            <div class="col-xs-4">
                <div class="bot-more-details">Developer</div>
                <div class="text-left">
                    <div id="botDeveloper"><?php echo $bot['developer'];?></div>
                    <div id="botEmail"><?php echo $bot['contact'];?></div>
                    <div id="botAddress"><?php echo $bot['address'];?></div>
                    <div id="botPostcode"><?php echo $bot['postcode'].' '. $bot['city'];?></div>
                    <div id="botNation"><?php echo $bot['nation'];?></div>
                </div>
            </div>
        </div>

    </div>
    
</div>