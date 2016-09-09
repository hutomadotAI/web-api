<form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">
    <div class="text-md">
    <div class="box box-body box-clean no-border flat no-shadow " >
         <?php echo $_POST['intent']?> - Intent
         <button class="btn btn-success flat pull-right" id="btnSaveIntent"  style="width: 120px;" alt="save intent" disabled>Save Intent</button>
    </div>
    </div>
</form>

