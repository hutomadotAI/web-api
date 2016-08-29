<div class="box box-solid box-clean flat no-shadow">
    <div class='box-header with-border'>
        <i class="fa fa-group text-light-blue"></i>
    <h3 class='box-title'>View all your AIs</h3>
    </div>

    <?php
        if ($response_getAIs['status']['code']===200) {

            echo('<div class="box-body table-responsive">');
            echo('<table class="table table">');
            echo('<tr disabled>');
            echo('<th class="text-center" style="border:0;">ID</th>');
            echo('<th class="text-center" style="border:0;">AI</th>');
            echo('<th style="border:0;">Status</th>');
            echo('<th class="text-center" style="border:0;">Description</th>');
            echo('<th style="border:0;">Date</th>');
            echo('</tr>');
            echo('<form method="POST" name="viewAllForm" action="./trainingAI.php">');

            foreach ($response_getAIs['ai_list'] as $bot) {
                echo('<tr>');
                echo('<td style="padding-top: 15px;">' . $bot['aiid'] . '</td>');
                echo('<td style="padding-top: 15px;">' . $bot['name'] . '</td>');

                switch ( $bot['ai_status']) {
                    case 0 :
                        echo('<td style="padding-top: 15px;"><span class="label label-primary">Queued</span></td>');
                        break;
                    case 1 :
                        echo('<td style="padding-top: 15px;"><span class="label label-warning">Training</span></td>');
                        break;
                    case 2 :
                        echo('<td style="padding-top: 15px;"><span class="label label-success">Trained</span></td>');
                        break;
                    case 3 :
                        echo('<td style="padding-top: 15px;"><span class="label label-warning">Stopping</span></td>');
                        break;
                    case 4 :
                        echo('<td style="padding-top: 15px;"><span class="label label-primary">Stopped</span></td>');
                        break;
                    case 5 :
                        echo('<td style="padding-top: 15px;"><span class="label label-danger">Limited</span></td>');
                        break;
                    default:
                        echo('<td style="padding-top: 15px;"><span class="label label-danger">Error</span></td>');
                }
                echo('<td style="padding-top: 15px;">' . $bot['description'] . '</td>');
                echo('<td style="padding-top: 15px;">' . $bot['created_on'] . '</td>');
                echo('<td style="padding-top: 8px;"><button id="btnSelect" name="ai" value="'.$bot['aiid'].'"  onClick="sendAIID()" class="btn btn-primary flat pull-right" style="margin-right: 5px; width: 115px;"><i class="fa fa-user" ></i> Details AI</button></td>');
                echo('<td style="padding-top: 8px;"><button id="btnPublish" value="'.$bot['aiid'].'"  onClick="publishAI.call()" class="btn btn-info flat pull-right"    style="margin-right: 5px; width: 115px;"><i class="fa fa-globe"></i> Publish AI</button></td>');
            }
            echo('</form>');
            echo('<tr>');
            echo('</table>');

        }else{

            echo('<div class="box-body">');
            echo('<div class="col-xs-12 center-block text-center">');
            echo('The list of your Ais is empty!');
            echo('</div>');
            echo('</div>');
        }
    unset($response_getAIs);
    ?>
</div>
</div>


