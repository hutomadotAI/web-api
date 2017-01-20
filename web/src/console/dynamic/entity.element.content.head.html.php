<input type="hidden" name="entity-name" id="entity-name" value="<?= $_POST['entity'] ?>">
<div class="box box-solid box-clean flat no-shadow unselectable">
    <div class="box-header no-border ">
        <i class="fa fa-sitemap text-yellow"></i>
        <div class="box-title"><b>Entity</b> > </span><b><?php echo $_POST['entity'] ?></b>
        </div>
        <div class="box-tools pull-right" style="padding-top:2px;">
            <button class="btn btn-success flat pull-right" id="btnSaveEntity" style="width: 120px;"
                    alt="save entity" onclick="saveEntity();RecursiveUnbind($('#wrapper'))">Save Entity
            </button>
        </div>
    </div>
</div>
