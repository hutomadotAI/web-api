<form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">
    <div class="box box-solid box-clean flat no-shadow" >

        <div class="box-header no-border">
            <i class="fa fa-commenting-o text-green"></i>
            <h3 class="box-title">Intents <span class="text-sm text-gray" style="padding: 0px 3px 0px 3px;"> > </span> <?php echo $_POST['intent']?></h3>
            <div class="box-tools pull-right"  style="top: 4px;">
                <button class="btn btn-success flat pull-right" id="btnSaveIntent"  style="width: 120px;" alt="save intent">Save Intent</button>
            </div>
        </div>
        
    </div>
</form>