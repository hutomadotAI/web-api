<div class="box box-solid box-clean flat no-shadow">
    <div class='box-header with-border'>
        <i class="fa fa-group text-light-blue"></i>
    <h3 class='box-title'>Your AIs</h3>
    </div>

    <?php
        if ($response_getAIs['status']['code']===200) {

            echo('<div class="box-body table-responsive">');
            echo('<table class="table table" id="listTable">');
            echo('<tr disabled>');
            echo('<th class="text-center" style="border:0;width:35%">ID</th>');
            echo('<th class="text-left" style="border:0;width:20%">AI Name</th>');
            echo('<th class="text-left" style="border:0;width:30%">Description</th>');
            echo('<th style="border:0;"width:15%>Status</th>');
            //echo('<th style="border:0;">Date</th>');
            echo('</tr>');

            foreach (array_reverse($response_getAIs['ai_list']) as $bot) {
                echo('<tr>');
                echo('<td style="padding-top: 15px;">' . $bot['aiid'] . '</td>');
                echo('<td style="padding-top: 15px;">' . $bot['name'] . '</td>');
                echo('<td style="padding-top: 15px;">' . $bot['description'] . '</td>');
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
               // echo('<td style="padding-top: 15px;">' . $bot['created_on'] . '</td>');
                echo('<td style="padding-top: 8px;"><button type="button" id="btnSelectAI"  value="'.$bot['aiid'].'"  onClick="sendAIID(this)" class="btn btn-primary flat pull-right" style="margin-right: 5px; width: 115px;"><i class="fa fa-user" ></i> View AI</button></td>');
                //echo('<td style="padding-top: 8px;"><button type="button" id="btnPublish" value="'.$bot['aiid'].'"  onClick="publishAI(this)" class="btn btn-info flat pull-right"    style="margin-right: 5px; width: 115px;"><i class="fa fa-globe"></i> Publish AI</button></td>');
                echo('<tr>');
            }


            echo('</table>');
            echo('<form method="POST" name="viewAllForm" action="./trainingAI.php">');
            echo('<input type="hidden" id="ai" name="ai" value="">');
            echo('</form>');

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


