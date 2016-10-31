<input type="hidden" id="intent-name" name="intent-name" value="<?= $_POST['intent'] ?>">
<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header no-border">
        <i class="fa fa-commenting-o text-green"></i>
        <div class="box-title"><b>Intent</b>
            <span class="text-sm text-gray" style="padding: 0px 3px 0px 3px;"> > </span>
            <span class="text-md text-blue"><b><?php echo $_POST['intent'] ?></b></span>
        </div>
        <div class="box-tools pull-right" style="top: 8px;">
            <button class="btn btn-success flat pull-right" id="btnSaveIntent" style="width: 130px;"
                    alt="save intent" onclick="saveIntent();RecursiveUnbind($('#wrapper'))">Save Intent
            </button>
        </div>
    </div>

</div>
