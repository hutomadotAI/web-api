<div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Add skills to your bot</b></div>
        <a data-toggle="collapse"  href="#collapseInfoNewAIBotstore">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="boxNewAIBotstore">
        <div class="alert alert-dismissable flat alert-info" id="containerMsgAlertNewAiBotstore" style="padding-bottom: 25px;">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
        <span id="msgAlertNewAiBotstore" >
        <dt>Hu:toma Bot Store</dt>
        <dd>
           You can boost your bot’s skills by combining it with other bots in our botstore.
        </dd>
        </span>
        </div>
        <input class="form-control flat no-shadow" id="botSearch" value="" placeholder="Search the Bot Store..." tabindex="0" onkeyup="searchBots(this.value)">
        <p></p>
    </div>

    <div id="collapseInfoNewAIBotstore" class="panel-collapse collapse">
        <div class="box-body no-margin">
            <div class="overlay center-block">
                <section class="content-info" >
                    <div class="box-body">
                        Bots are made available by our community and provide out of the box knowledge to your bot so you don't have to start from scratch.
                    </div>
                </section>
                <p></p>
            </div>
        </div>
    </div>

    <div class="box-footer">
        <button  style="width:100px" type="submit" id="btnBack" class="btn btn-primary flat pull-left" onCLick="backPage()"><b>Back</b></button>
        <button  style="width:100px" type="submit" id="btnNext" class="btn btn-success flat pull-right" onClick="wizardNext()"><b>Next</b></button>
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