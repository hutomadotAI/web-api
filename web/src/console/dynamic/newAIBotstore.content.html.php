<div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Add skills to your bot</b></div>
        <a data-toggle="collapse" href="#collapseInfoNewAIBotstore">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="boxNewAIBotstore">
        <div class="alert alert-dismissable flat alert-info no-margin" id="containerMsgAlertNewAiBotstore"
             style="padding-bottom: 25px;">
        <span id="msgAlertNewAiBotstore">
       <!-- <dt>Hu:toma Bot Store</dt> -->
        <dd>
           Combine up to 5 skills from the bots you've already purchased, and enhance your bot!
        </dd>
            <?php if (empty($purchasedBots)) { ?>
                <dd>
                You don't seem to have purchased any bots yet... Why don't you visit our <a
                            href="botstore.php">botstore</a> and acquire some bots to boost your bot's skills?
            </dd>
            <?php } else { ?>
                You can go to our <a href="botstore.php">botstore</a> and acquire more skills.
            <?php } ?>
        </span>
        </div>
    </div>

    <div id="collapseInfoNewAIBotstore" class="panel-collapse collapse">
        <div class="box-body no-margin">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        Bots are made available by our community and provide out of the box knowledge to your bot so you
                        don't have to start from scratch.
                    </div>
                </section>
                <p></p>
            </div>
        </div>
    </div>

    <div class="box-footer">
        <button style="width:100px" type="submit" id="btnBack" class="btn btn-primary flat pull-left"
                onCLick="backPage()"><b>Back</b></button>
        <button style="width:200px" type="submit" id="btnNext" class="btn btn-success flat pull-right"
                onClick="wizardNext()"><b>Save and go to your Bot</b></button>
    </div>
</div>


<p></p>
<h2></h2>
<p id="botsSearch"></p>
<form method="POST" name="newAIbotstoreform" action="./dynamic/saveAI.php">
    <input type="hidden" name="userActivedBots" id="userActivedBots" val="" style="display:none;">
</form>

<form method="POST" name="newAIbotstoreformGoBack">
    <input type="hidden" style="display:none;">
</form>