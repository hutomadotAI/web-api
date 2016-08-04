<div class="box box-solid flat no-shadow" style="border: 1px solid #d2d6de;">
    
    <div class="box-header with-border">
        <i class="fa fa-bar-chart-o"></i>
        <h3 class="box-title">Monitor Training</h3>
        <div class="box-tools pull-right" style="margin-top: 5px;margin-right: 7px;">
        Real time
        <div class="btn-group" id="realtime" data-toggle="btn-toggle" style="margin-left: 5px;">
        <button type="button" class="btn btn-default btn-xs flat active" data-toggle="on" style="border: 1px solid #d2d6de;">On</button>
        <button type="button" class="btn btn-default btn-xs flat" data-toggle="off" style="border: 1px solid #d2d6de;">Off</button>
        </div>
        </div>
    </div>
    
    <div class="box-body">
        <div id="interactive" style="height: 334px;"></div>
    </div>
</div>



  <?php
  //switch ($_SESSION['ALL_AI'][$_SESSION['AIID']]['status']) {
  /*
switch (2) {

    case -1 :
        echo ('<span class="label label-muted">empty</span>');
        break;
    case 0 :
        echo ('<span class="label label-primary">Queued</span>');
        break;
    case 1 :
        echo ('<span class="label label-warning">Training</span>');
        break;
    case 2 :
        echo ('<span class="label label-success">Trained</span>');
        break;
    case 3 :
        echo ('<span class="label label-warning">Stopping</span>');
        break;
    case 4 :
        echo ('<span class="label label-primary">Stopped</span>');
        break;
    case 5 :
        echo ('<span class="label label-danger">Limited</span>');
        break;
    default:
        echo ('<span class="label label-danger">Error</span>');
    }
  */
  ?>