<input type="hidden" id="intent-name" name="intent-name" value="<?= $_POST['intent'] ?>">
<input type="hidden" id="bot-status" name="bot-status" value="0" style="display:none;"/>
<div class="box box-solid box-clean flat no-shadow unselectable" style="padding-bottom:0px;">

    <div class="box-header no-border" style="padding: 10px 10px 0px 10px;">
        <div class="form-group no-margin">
            <div class="input-group">
                <div class="input-prefix-text">
                    <i class="fa fa-commenting-o text-green"></i>
                    <span><b> Intent </b></span><span class="text-md text-darkgray" style="padding-right:3px;">:</span>
                </div>
                <input type="text" class="flat no-shadow input-text-limited pull-left" value="<?= $_POST['intent'] ?>" readonly>
                <button class="input-postfix-button btn btn-success flat pull-right" id="btnSaveEntity" style="width: 130px; "
                        alt="save intent" onclick="saveIntent();RecursiveUnbind($('#wrapper'))">Save Intent
                </button>
            </div>
        </div>
    </div>


    <div class="box-body no-margin" id="boxExpression" style="padding-top:5px;padding-bottom:5px;">
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentElement"
             style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntentElement"></i>
            <span id="msgAlertIntentElement">Use intents to map what a user says and what action should be taken by your business logic.</span>
        </div>
    </div>
    
</div>

