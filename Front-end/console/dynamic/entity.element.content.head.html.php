<form method="POST" name="entityCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">

    <div class="box box-solid box-clean flat no-shadow" >
        <div class="box-header no-border ">
            <i class="fa fa-sitemap text-yellow"></i>
            <h3 class="box-title">Entity <span class="text-sm text-gray" style="padding: 0px 3px 0px 3px;"> > </span> <?php echo $_POST['entity']?></h3>
            <div class="box-tools pull-right"  style="top: 4px;">
                <button class="btn btn-success flat pull-right" id="btnSaveEntity"  style="width: 120px;" alt="save entity">Save Entity</button>
            </div>
        </div>
    </div>

</form>