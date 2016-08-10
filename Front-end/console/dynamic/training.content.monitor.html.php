<?php
function decodeAIState($state)
{
    switch ($state) {

        case -1 :
            return('<span class="label label-muted">Empty</span>');
            break;
        case 0 :
            return('<span class="label label-primary">Queued</span>');
            break;
        case 1 :
            return('<span class="label label-warning">Training</span>');
            break;
        case 2 :
            return('<span class="label label-success">Trained</span>');
            break;
        case 3 :
            return('<span class="label label-warning">Stopping</span>');
            break;
        case 4 :
            return('<span class="label label-primary">Stopped</span>');
            break;
        case 5 :
            return('<span class="label label-danger">Limited</span>');
            break;
        default:
            return('<span class="label label-danger">Error</span>');
    }
}
?>

<div class="box box-solid box-clean  flat no-shadow" >
    <div class="box-header no-border">
        <i class="fa fa-bar-chart-o text-success"></i>
        <h3 class="box-title">Training Monitor</h3>
        <a data-toggle="collapse"  href="#collapseMonitoring">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>
    <div class="box-body table-responsive no-padding">
        <table class="table ">
            <tr>
                <th class="text-center" style="width: 20%;" >State</th>
                <th class="text-center" style="width: 60%;">Progress</th>
                <th style="width: 10%;">Label</th>
            </tr>
            <tr>
                <td><?php echo ($_SESSION['ai_status']); ?></td>
                <td>
                    <div class="progress progress-xs progress-striped active" style="margin-top:9px;">
                        <div class="progress-bar progress-bar-success" style="width: 90%;"></div>
                    </div>
                </td>
                <td><span class="badge bg-green">90%</span></td>
            </tr>
        </table>

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
                need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>

            </div>
        </div>
    </div>

</div>