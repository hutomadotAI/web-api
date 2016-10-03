
    <?php
    function decodeAIState($state){
        switch ($state) {

            case 'training_queued' :
                return('<span class="label label-primary">Queued</span>');
                break;
            case 'preprocess_training_text' :
                return('<span class="label label-primary">Preprocess Text</span>');
                break;
            case 'preprocess_completed' :
                return('<span class="label label-primary">Preprocess Completed</span>');
                break;
            case 'ready_for_training' :
                return('<span class="label label-warning">Ready</span>');
                break;
            case 'start_training' :
                return('<span class="label label-warning" >Start</span>');
                break;
            case 'training_in_progress' :
                return('<span class="label label-primary">Training in progress</span>');
                break;
            case 'training_completed' :
                return('<span class="label label-success">Trained</span>');
                break;
            case 'delete_training' :
                return('<span class="label label-warning">Delete Training</span>');
                break;
            case 'stop_training' :
                return('<span class="label label-primary">Stopped</span>');
                break;
            case 'malformed_training_file' :
                return('<span class="label label-danger">Malformed Training File</span>');
                break;
            case 'training_stopped_maxtime' :
                return('<span class="label label-danger">Stopped Max Time</span>');
                break;
            default:
                return('<span class="label label-danger">Internal Error</span>');
        }
    }

    if (!$response_getAIs['status']['code']===200)
        include './dynamic/home.content.first.html.php';
    else {

        echo('<div class="box box-solid box-clean flat no-shadow">');
        echo('<div class="box-header with-border">');
        echo('<i class="fa fa-group text-light-blue"></i>');
        echo('<h3 class="box-title">Your AIs</h3>');
        echo('</div>');


        echo('<div class="box-body table-responsive">');
        echo('<table class="table table" id="listTable">');
        echo('<tr disabled>');
        echo('<th class="text-center" style="border:0;width:35%">ID</th>');
        echo('<th class="text-left" style="border:0;width:20%">AI Name</th>');
        echo('<th class="text-left" style="border:0;width:25%">Description</th>');
        echo('<th style="border:0;"width:15%>Status</th>');
        echo('<th style="border:0;"width:5%></th>');
        //echo('<th style="border:0;">Date</th>');
        echo('</tr>');

        foreach (array_reverse($response_getAIs['ai_list']) as $bot) {
            echo('<tr>');
            echo('<td style="padding-top: 15px;">' . $bot['aiid'] . '</td>');
            echo('<td style="padding-top: 15px;">' . $bot['name'] . '</td>');
            echo('<td style="padding-top: 15px;">' . $bot['description'] . '</td>');
            echo('<td style="padding-top: 15px;">' . decodeAIState($bot['ai_status']) . '</td>');
            // echo('<td style="padding-top: 15px;">' . $bot['created_on'] . '</td>');
            echo('<td style="padding-top: 8px;"><button type="button" id="btnSelectAI"  value="' . $bot['aiid'] . '"  onClick="sendAIID(this)" class="btn btn-primary flat pull-right" style="margin-right: 5px; width: 115px;"><i class="fa fa-user" ></i> View AI</button></td>');
            //echo('<td style="padding-top: 8px;"><button type="button" id="btnPublish" value="'.$bot['aiid'].'"  onClick="publishAI(this)" class="btn btn-info flat pull-right"    style="margin-right: 5px; width: 115px;"><i class="fa fa-globe"></i> Publish AI</button></td>');
            echo('<tr>');
        }


        echo('</table>');
        echo('<form method="POST" name="viewAllForm" action="./trainingAI.php">');
        echo('<input type="hidden" id="ai" name="ai" value="">');
        echo('</form>');


        echo('</div>');
        echo('</div>');
    }

    unset($response_getAIs);
    ?>
