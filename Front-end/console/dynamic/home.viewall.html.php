<?php

    function decodeAIState($state){
        switch ($state) {
            case 'STOPPED' :
                return('<span class="label label-primary">Stopped</span>');
                break;
            case 'NOT_STARTED' :
                return('<span class="label label-warning">Not started</span>');
                break;
            case 'QUEUED' :
                return('<span class="label label-warning">Queued</span>');
                break;
            case 'IN_PROGRESS' :
                return('<span class="label label-primary">in progress</span>');
                break;
            case 'STOPPED_MAX_TIME' :
                return('<span class="label label-warning" >Stopped Max Time</span>');
                break;
            case 'COMPLETED' :
                return('<span class="label label-success">Completed</span>');
                break;
            case 'ERROR' :
                return('<span class="label label-danger">Error</span>');
                break;
            case 'MALFORMEDFILE' :
                return('<span class="label label-danger">Malformed</span>');
                break;
            default:
                return('<span class="label label-danger">Undefined</span>');
        }
    }
    if (!isset($response_getAIs) || !(array_key_exists("ai_list",$response_getAIs)))
        include './dynamic/home.content.first.html.php';
    else {
        echo('<div class="box box-solid box-clean flat no-shadow">');
        echo('<div class="box-header with-border">');
        echo('<div class="box-title"><b>Your AIs</b></div>');
        echo('</div>');

        echo('<div class="box-body table-responsive no-border">');
        echo('<table class="table table" id="listTable">');
        echo('<tr disabled>');
        echo('<th class="text-center" style="border:0;width:35%">ID</th>');
        echo('<th class="text-left" style="border:0;width:20%">AI Name</th>');
        echo('<th class="text-left" style="border:0;width:25%">Description</th>');
        echo('<th style="border:0;"width:15%>Training</th>');
        echo('<th style="border:0;"width:5%></th>');
        //echo('<th style="border:0;">Date</th>');
        echo('</tr>');

        $aiList = $response_getAIs['ai_list'];
        foreach (array_reverse($aiList) as $bot) {
            echo('<tr>');
            echo('<td style="padding-top: 15px;">' . $bot['aiid'] . '</td>');
            echo('<td style="padding-top: 15px;">' . $bot['name'] . '</td>');
            echo('<td style="padding-top: 15px;">' . $bot['description'] . '</td>');
            echo('<td style="padding-top: 15px;">' . decodeAIState($bot['ai_status']) . '</td>');
            // echo('<td style="padding-top: 15px;">' . $bot['created_on'] . '</td>');
            echo('<td style="padding-top: 8px;"><button type="button" id="btnSelectAI"  value="' . $bot['aiid'] . '"  onClick="sendAIID(this)" class="btn btn-primary flat pull-right" style="margin-right: 5px; width: 115px;"><b> <span class="fa fa-search"></span> View AI </b></button></td>');
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
