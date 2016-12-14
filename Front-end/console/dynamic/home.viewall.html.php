<div class="box box-solid box-clean flat no-shadow">
    <div class="box-header with-border">
        <div class="box-title"><b>Your AIs</b></div>
    </div>

    <div class="box-body table-responsive no-border" style="padding-top:0px;">
        <table class="table dataTable" id="tableAi">
            <thead>
            <tr disabled>
            <th class="text-center" style="border:0; width:35%">ID</th>
            <th class="text-left" style="border:0; width:20%">AI Name</th>
            <th class="text-left" style="border:0; width:25%">Description</th>
            <th class="text-center" style="border:0; width:15%">Training</th>
            <th style="border:0; width:5%"></th>
            <th style="border:0; width:5%"></th>
            </tr>
            </thead>
            <tbody id="tableAiList">
            </tbody>
        </table>
       
        <form method="POST" name="viewAllForm" action="./trainingAI.php">
            <input type="hidden" id="ai" name="ai" value="">
        </form>
        <form method="POST" name="publishForm" action="">
            <input type="hidden" id="aiid" name="aiid" value="">
        </form>
</div>

    

