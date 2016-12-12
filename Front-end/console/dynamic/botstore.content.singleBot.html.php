<div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">
    <div class="box-body" id="botcard">

        <div class="col-xs-4 no-padding" id="botIcon">
            <div class="bot-circle-icon" id="botIcon">
            </div>
        </div>

        <div class="col-xs-8 bot-info" id="botInfo">
            <div class="row no-margin">
                <div class="col-xs-6 bot-title" id="botTitle">              <!--title-->
                    <?php echo $singleAI['name'];?>
                </div>
                <div class="col-xs-6 bot-developer text-primary" id="botDeveloper">      <!--developer-->
                    dev App corporation
                </div>
            </div>

            <div class="row no-margin">
                <div class="col-xs-7 bot-description" id="botDescription">  <!--description-->
                    <?php echo $singleAI['description'];?>
                </div>
                <div class="col-xs-5 bot-star" id="botDeveloper">           <!--rating-->
                   99 users
                    <div class="star-rating text-right">
                        <div class="star-rating__wrap">
                            <?php
                                for ($i=5; $i>0; $i--) {
                                    //need to get $singleAI['rating']
                                    if ($i==intval(4.4)) {
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
                <div class="col-xs-12 no-padding nbot-buy">
                    <button class="btn btn-success btn-lg pull-right flat" id="btnBuyBot"> <b>Buy new Bot</b> <span class="fa fa-arrow-circle-right"></span></button>
                </div>
            </div>
        </div>
    </div>

    <div class="box-body bot-padding flat">
        <div class="col-xs-12 no-padding bot-video flat" id="botVideo">
            <div class="overlay center-block flat">
                <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                    <iframe
                        src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                        frameborder="0" allowfullscreen>
                    </iframe>
                </div>
            </div>
        </div>
    </div>

    <div class="box-body bot-padding ">
        <div class="col-xs-12 text-gray bot-detail" id="botDetail">
                A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart.
                I am alone, and feel the charm of existence in this spot, which was created for the bliss of souls like mine. I am so happy,
                my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents.
                I should be incapable of drawing a single stroke at the present moment; and yet I feel that I never was a greater artist than now.
        </div>
    </div>

    <div class="box-footer">
        <div class="row bot-padding">
            <div class="col-xs-12 bot-footer" id="botMoreDetail">
                More details
            </div>
        </div>
        <div class="row">
            <div class="col-xs-4" id="botUpdated">
                <div class="bot-more-details">Last Update</div>
            </div>
            <div class="col-xs-4" id="botDimension">
                <div class="bot-more-details">Dimension</div>
            </div>
            <div class="col-xs-4" id="botInstallation">
                <div class="bot-more-details">Installation</div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-4" id="botVersion">
                <div class="bot-more-details">Version</div>
            </div>
            <div class="col-xs-4" id="botClassification">
                <div class="bot-more-details">Classification</div>
            </div>
            <div class="col-xs-4" id="botDeveloper">
                <dd>Visita il sito web</dd>
                <dd>Invia un'email a support@buzzfeed.com</dd>
                <dd>Norme sulla privacy</dd>
                <dd>111 E 18th St</dd>
                <dd>Floor 16</dd>
                <dd>New York, NY 10003</dd>
            </div>
        </div>
    </div>
    
</div>