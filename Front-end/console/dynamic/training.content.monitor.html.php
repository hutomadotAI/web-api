<?php

    $singleAI = \hutoma\console::getSingleAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);

    if ($singleAI['status']['code'] !== 200) {
        unset($singleAI);
        header('Location: ./error.php?err=18');
        exit;
    }
    // TO DO loader but API CALL
    if(\hutoma\console::getAiTrainingFile($singleAI['aiid'])== null)
        $training_file = false;
    else
        $training_file = true;

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
                return('<span class="label label-dangel">Malformed</span>');
                break;
            default:
                return('<span class="label label-danger">Undefined</span>');
        }
    }
?>



<div class="box box-solid box-clean flat no-shadow" id="box_monitor">
    <div class="box-header with-border">
        <i class="fa fa-bar-chart-o text-success"></i>
        <div class="box-title"><b>Training Status</b></div>

        <a data-toggle="collapse"  href="#collapseMonitoring">
            <div class=" pull-right"><i class="fa fa-info-circle text-sm text-yellow"></i> more info

            </div>
        </a>
    </div>

    <div class="box-body table-responsive no-border">

        <table class="table">
            <tr>
                <th class="text-center no-border" style="width: 20%;">Training Phase</th>
                <th class="text-center no-border">Progress</th>
                <th class="text-center no-border" style="width: 120px;">Completed</th>
            </tr>
            <tr>
                <!-- Phase1 is the "time" to wait for upload training file -->
                <td class="text-center" id="status-upload-file">phase 1</td>
                <td>
                    <div class="progress progress-xs progress-striped active" id="progress-upload-file-action" style="margin-top:9px;">
                       <div class="progress-bar progress-bar-primary" id="progress-upload-file" style="width:0;"></div>
                    </div>
                </td>
                <td class="text-center"><span id="status-bagde-upload" class="badge btn-primary">0%</span></td>
            </tr>

            <tr id="trainingbar" hidden>
                <!-- Phase2 is the "time" to monitoring the training error progress -->
                <td class="text-center" id="status-training-file">phase 2</td>
                <td>
                    <div class="progress progress-xs progress-striped active" style="margin-top:9px;">
                        <div class="progress-bar progress-bar-success"  id="progress-training-file" style="width:0;"></div>
                    </div>
                </td>
                <td class="text-center" style="width: 120px;"><span id="status-bagde-training" class="badge btn-success">0%</span></td>
            </tr>
        </table>

        <div id="container_startstop" style="display: none;">
            <table class="table no-margin">
                <td class="text-left no-border" style="padding-bottom: 0;">
                    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertProgressBar" style="margin-bottom: 0; padding-right:0;">
                        <i class="icon fa fa-check" id="iconAlertProgressBar"></i>
                        <span id="msgAlertProgressBar">You can now talk to your AI.</span>
                    </div>
                </td>
                <td class="text-center no-border" style="width: 120px;">
                    <a type="submit" class="btn btn-app flat" id="startstop-button" value="_stop" style="margin-left: 0;">
                        <i class="fa fa-stop no-margin" id="startstop-icon"></i>
                        <span class="text-sm" id="text-startstop">stop training</span>
                    </a>
                </td>
            </table>
            


        </div>

    </div>

    <div id="collapseMonitoring" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content-info" >
                    <div class="box-body">
                        <dl class="dl-horizontal">
                            Training consists of two main phases:<br /><br/>
                            <ul>
                                <li style="text-align:justify"><b>Phase 1 (phrase level learning)</b><br>During the first phase of training, your AI will be learning to respond to end users by leveraging the pre-packaged answers that are contained in your training file. In other words, the AI will understand what of the pre-made answers wil be more appropiate to use. Phrase level learning is usually quicker than Phase 2 and it allows you to interact with your AI almost right away.</li><br/>
                                <li style="text-align:justify"><b>Phase 2 (word level learning)</b><br>Word level learning teaches your AI to recognize the underlying rules behind the sample conversations you provided. This phase can take days or even several weeks depending on how big your training file is. However once completed, it will allow the AI to formulate completely new answers that will go beyond your pre-packaged answers. Phase 2 responses are usually available after few minutes of training but they require more time to get consistent with the training you provided.</li>

                            </ul>
                        </dl>
                    </div>
                </section>
                <p></p>

            </div>
        </div>
    </div>

</div>
<script>
    var status = '<?php echo $singleAI['ai_status']; ?>';
    var training_file = <?php echo json_encode($training_file);?>;
    var error = <?php echo $singleAI['deep_learning_error']; ?>;
</script>