<div class="box box-solid box-clean flat no-shadow" id="box_monitor">

    <div class="box-header with-border">
        <i class="fa fa-bar-chart-o text-success"></i>
        <div class="box-title"><b>Training Status</b></div>
        <a data-toggle="collapse"  href="#collapseMonitoring">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
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
            <tr id="pretrainingbar">
                <!-- Phase1 is the "time" to wait for upload training file -->
                <td class="text-center" id="status-upload-file">phase 1</td>
                <td>
                    <div class="progress progress-xs progress-striped active" id="progress-upload-file-action" style="margin-top:9px;">
                        <div class="progress-bar progress-bar-primary" id="progress-upload-file" value="0" style="width:0;"></div>
                    </div>
                </td>
                <td class="text-center"><span id="status-badge-upload" class="badge btn-primary">0%</span></td>
            </tr>

            <tr id="trainingbar">
                <!-- Phase2 is the "time" to monitoring the training error progress -->
                <td class="text-center" id="status-training-file">phase 2</td>
                <td>
                    <a data-toggle="collapse"  href="#collapseChartTrainingError">
                        <div class="text-center">
                            See training chart details
                            <i class="fa fa-plus-circle text-sm text-yellow"></i>
                        </div>
                    </a>
                </td>
                <td class="text-center" style="width: 120px;"><span id="status-badge-training" class="badge btn-success">0%</span></td>
            </tr>
        </table>



        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertProgressBar" style="margin-bottom: 0; padding-right:0;">
            <i class="icon fa fa-check" id="iconAlertProgressBar"></i>
            <span id="msgAlertProgressBar">Training not started. Please upload training data.</span>
        </div>
    </div>

    <div id="collapseMonitoring" class="panel-collapse collapse">
        <div class="box-body" style="padding-top:0;">
            <div class="overlay center-block">
                <section class="content-info">
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
                <section class="content-info" style="padding-left:15px;">
                    need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                </section>
            </div>
        </div>
    </div>
    <div class="box-header no-border" style="padding-top: 0px;padding-bottom: 0px;">
        <a data-toggle="collapse"  href="#collapseChartTrainingError">
            <div class=" pull-left">
                <i class="fa fa-plus-circle text-sm text-yellow"></i>
                See training chart details
            </div>
        </a>
        <div class=" pull-right">
            Precision : <span id="show-error"></span>
        </div>
    </div>

    <div class="row no-padding">
        <div class="col-xs-12">
            <div class="box-body table-responsive no-border">
                <div id="collapseChartTrainingError" class="panel-collapse collapse">
                 <!--   <button class="btn btn-success btn-sm center-block flat" id="zoomIn"><span class="fa fa-plus"></span></button> -->
                    <div id="interactive" style="width: 100%;height: 150px;"></div>
                </div>
            </div>
        </div>
    </div>
</div>

