<div class="box-header with-border unselectable">
    <i class="fa fa-shopping-cart text-green"></i>
    <div class="box-title"><b>Selected bots from the store</b></div>
    <a data-toggle="collapse"  href="#collapseInfoDomains">
        <div class=" pull-right">more info
            <i class="fa fa-info-circle text-sm text-yellow"></i>
        </div>
    </a>
</div>

<div id="collapseInfoDomains" class="panel-collapse collapse">
    <div class="box-body flat" style="padding-bottom:0px;">
        <div class="overlay center-block">
            <section class="content-info">
                <div class="box-body">
                    <dl class="dl-horizontal no-margin" style="text-align:justify">
                        You can add new capabilities to your bot by selecting pre-trained bots from our botstore.
                   </dl>
                </div>
            </section>
        </div>
    </div>
</div>

<div class="box-body unselectable" id="boxAiSkill">
    <input class="form-control flat no-shadow" id="searchInputDomains" value="" placeholder="Search selected skills..." tabindex="0" onkeyup="searchBots(this.value)">
    <p></p>
    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertAiSkill" style="margin-bottom:0px;">
        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
        <i class="icon fa fa-check" id="iconAlertAiSkill"></i>
        <span id="msgAlertAiSkill">You can power up your bot by combining and mixing existing bots from out botstore.</span>
    </div>
</div>

<div class="box-footer unselectable">
    <button style="width:100px" type="submit" id="btnAiSkillCancel" class="btn btn-primary flat pull-left">cancel</button>
    <button style="width:100px" type="submit" id="btnAiSkillSave" class="btn btn-success flat pull-right">save</button>
</div>