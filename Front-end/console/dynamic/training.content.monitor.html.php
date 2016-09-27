<?php

    $singleAI = \hutoma\console::getSingleAI(\hutoma\console::getDevToken(), $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);

    if ($singleAI['status']['code'] !== 200) {
        unset($singleAI);
        header('Location: ./error.php?err=18');
        exit;
    }

    function decodeAIState($state){
        switch ($state) {

            case -1 :
                return('<span class="label label-muted">Empty</span>');
                break;
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
                return('<span class="label label-warning">Start</span>');
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
?>



<div class="box box-solid box-clean flat no-shadow" >
    <div class="box-header with-border">
        <i class="fa fa-bar-chart-o text-success"></i>
        <h3 class="box-title">Training Status</h3>

        <a data-toggle="collapse"  href="#collapseMonitoring">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body table-responsive">

        <table class="table">
            <tr>
                <th class="text-center no-border" style="width: 20%;" >Training Phase</th>
                <th class="text-center no-border" style="width: 60%;">Progress</th>
                <th class="text-center no-border" style="width: 10%;">Completed</th>
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
                <td class="text-center"><span id="status-bagde-training" class="badge btn-success">0%</span></td>
            </tr>
        </table>


        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertProgressBar" style="display: none">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertProgressBar"></i>
            <span id="msgAlertProgressBar"></span>
        </div>


    </div>

    <div id="collapseMonitoring" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content bg-gray-light" >
                    <div class="box-body">
                        <dl class="dl-horizontal">
                            <dt>Description Actions</dt>
                            <dd>Before start training process, y.</dd>
                            <dt>Euismod</dt>
                            <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                            <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                            <dt>Malesuada porta</dt>
                            <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                            <dt>Felis euismod semper eget lacinia</dt>
                            <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                        </dl>
                    </div>
                </section>
                <p></p>
                need help? check our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>

            </div>
        </div>
    </div>

</div>
<script>
    var status = '<?php echo $singleAI['ai_status']; ?>';
    var error = <?php echo $singleAI['deep_learning_error']; ?>;
</script>