<form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">
    <div class="box box-solid box-clean flat no-shadow">

        <div class="box-header no-border">
            <i class="fa fa-commenting-o text-green"></i>
            <div class="box-title"><b>Intent</b>
                <span class="text-sm text-gray" style="padding: 0px 3px 0px 3px;"> > </span>
                <span class="text-md text-blue"><b><?php echo $_POST['intent']?></b></span>
            </div>
            <div class="box-tools pull-right"  style="top: 8px;">
                <button class="btn btn-success flat pull-right" id="btnSaveIntent"  style="width: 130px;" alt="save intent">Save Intent</button>
            </div>
        </div>
        
    </div>
</form>