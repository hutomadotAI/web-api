<div class="box-body bot-card" id="botcard">

    <div class="col-xs-4 no-padding <?php echo $bot->getWidgetColor();?> ">
        <div class="bot-icon" id="botIcon">
            <i class="<?php echo $bot->getIconPath();?>" style="padding-top:45px;"></i>
        </div>
    </div>

    <div class="col-xs-8 bot-info">
        <div class="row no-margin">
            <div class="col-xs-6 bot-title text-white" id="botTitle">   <!--title-->
                <?php echo $bot->getName();?>
            </div>
            <div class="col-xs-6 bot-badge" id="botBagde">      <!--developer-->
                <i class="<?php echo $bot->getBadgeIcon();?> text-aqua"></i> <?php echo $bot->getBadge();?>
            </div>
        </div>

        <div class="row no-margin">
            <div class="col-xs-7 bot-description" id="botDescription">  <!--description-->
                <?php echo $bot->getDescription();?>
            </div>
            <div class="col-xs-5 bot-star"  id="botUsers">           <!--rating-->
                <?php echo $bot->getUsers();?> users
                <div class="star-rating text-right">
                    <div class="star-rating__wrap">
                        <?php
                        for ($i=5; $i>0; $i--) {
                            if ($i==intval($bot->getRating())) {
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
                <i class="fa fa-info-circle text-sm text-yellow"></i> <?php echo $bot->getAlarmMessage();?>
            </div>
        </div>
        <div class="row no-margin">
            <div class="col-xs-12 bot-other">
            </div>
        </div>

        <div class="row no-margin">
            <div class="col-xs-6 no-padding" id="botTitle">   <!--title-->
                <div class="row no-margin bot-licence">
                    licence <?php echo strtoupper($bot->licenceTypeToString($bot->getLicenceType()));?>
                </div >
                <div class="row no-margin bot-price">
                    <div class="pull-left text-orange">price <span class="text-orange"><?php echo number_format($bot->getLicenceFee(), 2, '.', ''); ?></span> <span class="bot-badge no-padding text-orange">£</span></div>
                </div >
            </div>
            <div class="col-xs-6 bot-buy">
                <button class="btn btn-success pull-right flat" id="btnBuyBot" data-toggle="modal" data-target="#buyBot">
                    <b>Buy new Bot</b>
                    <span class="fa fa-arrow-circle-right"></span>
                </button>
            </div>
        </div>
    </div>
</div>