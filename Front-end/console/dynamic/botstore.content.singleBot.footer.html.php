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
                <?php echo $bot->getUpdate();?>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details" id="botClassification">Classification</div>
            <div class="text-left">
                <?php echo $bot->getClassification();?>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details">Version</div>
            <div class="text-left" id="botVersion">
                <?php echo $bot->getVersion();?>
            </div>
        </div>
    </div>

    <div class="row no-margin">
        <div class="col-xs-4">
            <div class="bot-more-details">Offer by</div>
            <div class="text-left" id="botCompany">
                <?php echo $bot->developer->getCompany();?>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details">Permissions</div>
            <div class="text-left" id="botPermission">
                <a class="dev-link" href="<?php echo $bot->getPermissionLink();?>" rel="nofollow" target="_blank">View details</a>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details">Activations</div>
            <div class="text-left" id="botActivations">
                <?php echo $bot->rangeActivation($bot->getActivations());?>
            </div>
        </div>
    </div>

    <div class="row no-margin">
        <div class="col-xs-4">
            <div class="bot-more-details">Report</div>
            <div class="text-left" id="botReport">
                <a class="dev-link" href="<?php echo $bot->getReport();?>" rel="nofollow" target="_blank">Report as inappropriate</a>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details">Privacy</div>
            <div class="text-left" id="botPrivacyPage">
                <a class="dev-link" href="<?php echo $bot->getPrivacyLink();?>" rel="nofollow" target="_blank">View Privacy Policy</a>
            </div>
        </div>
        <div class="col-xs-4">
            <div class="bot-more-details">Developer</div>
            <div class="text-left">
                <a class="dev-link" href="<?php echo $bot->developer->getWebsite();?>" rel="nofollow" target="_blank">Visit Website</a>
                <div id="botDeveloper"><?php echo $bot->developer->getName();?></div>
                <div id="botEmail"><?php echo $bot->developer->getEmail();?></div>
                <div id="botAddress"><?php echo $bot->developer->getAddress();?></div>
                <div id="botPostcode"><?php echo $bot->developer->getPostcode().' '. $bot->developer->getCity();?></div>
                <div id="botNation"><?php echo $bot->developer->getNation();?></div>
            </div>
        </div>
    </div>

</div>