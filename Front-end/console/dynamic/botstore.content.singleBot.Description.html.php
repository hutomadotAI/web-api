<div class="box-body">
    <div class="row no-margin">
        <div class="col-xs-12 bot-h2">
            Description
        </div>
    </div>
    <div class="row no-margin" style="padding-bottom:10px;">
        <div class="col-xs-12 bot-detail" id="botLongDescription">
            <?php echo $bot->getLongDescription();?>
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
                <div class="text-gray"><?php echo $bot->getUsescase();?></div>
            </div>
        </div>
    </div>
</div>