<form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">

    <div class="box box-solid box-clean flat no-shadow" >
        <div class="box-header no-border ">
                <h3 class="box-title"> <?php echo $_POST['intent']?> - Intent</h3>
                <div class="box-tools pull-right"  style="top: 4px;">
                    <button class="btn btn-success flat pull-right" id="btnSaveIntent"  style="width: 120px;" alt="save intent" disabled>Save Intent</button>
                </div>
        </div>
    </div>

</form>