<div class="box-header with-border">
    <i class="fa fa-shopping-cart text-green"></i>
    <div class="box-title"><b>Selected Bot Store AIs</b></div>
    <a data-toggle="collapse"  href="#collapseInfoDomains">
        <div class=" pull-right">more info
            <i class="fa fa-info-circle text-sm text-yellow"></i>
        </div>
    </a>
</div>

<div class="box-body" id="boxAiSkill" style="padding-bottom:0px;">
    <p></p>
    <input class="form-control flat no-shadow" id="searchInputDomains" value="" placeholder="Search selected skills..." tabindex="0" onkeyup="searchDomain(this.value)">
    <p></p>
    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertAiSkill" style="margin-bottom:0px;">
        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
        <i class="icon fa fa-check" id="iconAlertAiSkill"></i>
        <span id="msgAlertAiSkill">You can power up your AI by combining and mixing existing AIs from out Bot Store.</span>
    </div>
</div>

<div id="collapseInfoDomains" class="panel-collapse collapse">
    <div class="box-body">
        <div class="overlay center-block">
            <section class="content-info" style="padding-bottom:0px; padding-top:0px;">
                <div class="box-body">
                   <b>AI Skills</b><br/>
                  You can add new capabilities to your AI by selecting pre-trained neural networks from our Bot Store.
                </div>
            </section>
        </div>
    </div>
</div>

<div class="box-footer no-border">
    <!--<button style="width:100px" type="submit" id="btnAiSkillReset" class="btn btn-primary flat pull-left">reset</button>-->
    <button style="width:100px" type="submit" id="btnAiSkillCancel" class="btn btn-primary flat pull-left">cancel</button>
    <button style="width:100px" type="submit" id="btnAiSkillSave" class="btn btn-success flat pull-right">save</button>
</div>