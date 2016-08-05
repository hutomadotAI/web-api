<div class="box box-solid box-primary flat no-shadow">
    <div class='box-header with-border'>
        <i class="fa fa-group"></i>
    <h3 class='box-title'>View all your AIs</h3>
    </div>

    <?php

        $dev_token = \hutoma\console::getDevToken();
        $array = \hutoma\console::getAIs($dev_token);
        unset($dev_token);
        if ($array['status']['code']===200) {

            echo('<div class="box-body table-responsive">');
            echo('<table class="table table-hover">');
            echo('<tr>');
            echo('<th>ID</th>');
            echo('<th>AI</th>');
            echo('<th>Status</th>');
            echo('<th>Description</th>');
            echo('<th>Date</th>');
            echo('</tr>');
            echo('<form method="POST" id="viewAllAIsform" action="./trainingAI.php">');

            foreach ($array['ai_list'] as $bot) {
                echo('<tr>');
                echo('<td>' . $bot['aiid'] . '</td>');
                echo('<td>' . $bot['name'] . '</td>');

                switch ( $bot['ai_status']) {
                    case 0 :
                        echo('<td><span class="label label-primary">Queued</span></td>');
                        break;
                    case 1 :
                        echo('<td><span class="label label-warning">Training</span></td>');
                        break;
                    case 2 :
                        echo('<td><span class="label label-success">Trained</span></td>');
                        break;
                    case 3 :
                        echo('<td><span class="label label-warning">Stopping</span></td>');
                        break;
                    case 4 :
                        echo('<td><span class="label label-primary">Stopped</span></td>');
                        break;
                    case 5 :
                        echo('<td><span class="label label-danger">Limited</span></td>');
                        break;
                    default:
                        echo('<td><span class="label label-danger">Error</span></td>');
                }
                echo('<td>' . $bot['description'] . '</td>');
                echo('<td>' . $bot['created_on'] . '</td>');
                echo('<td><button type="button" name="btnSelect" value="'.$bot['aiid'].'"  onClick="sendAIID(this.value)" class="btn btn-primary pull-right" id="btn_select" style="margin-right: 5px; width: 115px;"><i class="fa fa-user"></i> details AI</button></td>');
            }

            unset($array);

            echo('</form>');
            echo('<tr>');
            echo('<td></td><td></td><td></td><td></td><td></td>');
            echo('<td><a href="./newAI.php"><div class="btn btn-success pull-right" style="margin-right: 5px; width: 115px;"><i class="fa fa-user-plus"></i> add new AI</div></a></td>');
            echo('</tr>');
            echo('</table>');

        }else{

            echo('<div class="box-body">');
            echo('<div class="col-xs-12 center-block text-center">');
            echo('The list of your Ais is empty!');
            echo('</div>');
            echo('</div>');
        }
    ?>
</div>
</div>

<script>
function sendAIID(aiid){
    var input = document.createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("name", "aiid");
    input.setAttribute("value", aiid);
    
    document.getElementById("viewAllAIsform").appendChild(input);
    document.getElementById("viewAllAIsform").submit();
}
</script>