<div class="box box-solid box-clean flat no-shadow unselectable" id="box_monitor">

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

            <tr id="pretrainingbar"></tr>
            <tr id="trainingbar"></tr>

        </table>

        <span id="msgAlertBox"></span>
        
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
                    need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>support@email.com</a>
                </section>
            </div>
        </div>
    </div>

    <div id="chart-details">
        <div class="box-header no-border" style="padding-top: 0px;padding-bottom: 0px;">
            <div class="text-gray pull-right" style="padding-bottom:5px;">
                Learning error : <span id="show-error"></span>
            </div>
        </div>
    </div>
    <div class="box-footer" id="chart-details-footer" hidden>
        <div class="alert alert-dismissable flat alert-info no-margin no-padding">
            <div class="text-muted text-center no-margin no-padding" >
                Your AI is trained, when the learning error reaches a number that is close to 0
            </div>
        </div>
    </div>
</div>
