<input type="hidden" name="entity-name" id="entity-name" value="<?= $_POST['entity'] ?>">
<div class="box box-solid box-clean flat no-shadow unselectable">
    
    <div class="box-header no-border" style="padding: 10px 10px 5px 10px;">
        <div class="form-group no-margin">
            <div class="input-group">
                <div class="input-prefix-text">
                    <i class="fa fa-sitemap text-yellow"></i>
                    <span><b> Entity </b></span><span class="text-md text-darkgray"> > </span>
                </div>
                <input type="text" class="flat no-shadow input-text-limited pull-left" value="@<?= $_POST['entity'] ?>" readonly>
                <button class="input-postfix-button btn btn-success flat pull-right" id="btnSaveEntity" style="width: 130px; "
                        alt="save entity" onclick="saveEntity();RecursiveUnbind($('#wrapper'))">Save Entity
                </button>
            </div>
        </div>
    </div>
    
</div>
