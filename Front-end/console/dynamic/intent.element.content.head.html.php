<input type="hidden" id="intent-name" name="intent-name" value="<?= $_POST['intent'] ?>">
<div class="box box-solid box-clean flat no-shadow" style="padding-bottom:0px;">

    <div class="box-header no-border">
        <i class="fa fa-commenting-o text-green"></i>
        <div class="box-title"><b>Intent</b>
            <span class="text-sm text-gray" style="padding: 0px 3px 0px 3px;"> > </span><b><?php echo $_POST['intent'] ?></b></span>
        </div>
        <div class="box-tools pull-right" style="top: 8px;">
            <button class="btn btn-success flat pull-right" id="btnSaveIntent" style="width: 130px;"
                    alt="save intent" onclick="saveIntent();RecursiveUnbind($('#wrapper'))">Save Intent
            </button>
        </div>
    </div>

    <div class="col-md-12" style="padding-top:5px; padding-left: 10px;padding-right: 10px;">
    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentElement" style="margin-bottom:10px;">
        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
        <i class="icon fa fa-check" id="iconAlertIntentElement"></i>
        <span id="msgAlertIntentElement">Give the AI a list of BLA BLA BLA BLA BLA BLA BLA.</span>
    </div>
    </div>

</div>
