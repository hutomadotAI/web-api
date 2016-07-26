<div class="box box-solid box-clean flat no-shadow">

    <a data-toggle="collapse" data-parent="#accordion" href="#collapseDiagram">
    <div class="box-header with-border">
        <h3 class="box-title">Monitor Training</h3>
        <div class="box-tools pull-right">
        <button class="btn btn-box-tool" ><i class="fa fa-minus"></i></button>
        </div>
    </div>
    </a>

    <div id="collapseDiagram" class="panel-collapse collapse">
    <div class="box-header with-border">


            <div class="btn-group pull-left" id="realtime" data-toggle="btn-toggle" >
                <button type="button" class="btn btn-default btn-md" data-toggle="tooltip" title="stop training"><i class="fa fa-stop"></i></button>
                <button type="button" class="btn btn-default btn-md" data-toggle="tooltip" title="pause training"><i class="fa fa-pause"></i></button>
                <button type="button" class="btn btn-default btn-md" data-toggle="tooltip" title="start training"><i class="fa fa-play"></i></button>
            </div>

            <div class="btn-group pull-right" id="realtime" data-toggle="btn-toggle">
                <div class="center-block text-center">Real Time
                    <button type="button" class="btn btn-default btn-md" data-toggle="on">On</button>
                    <button type="button" class="btn btn-default btn-md active" data-toggle="off">Off</button>
                </div>
            </div>


    </div>



    <div class="box-body">
          <div id="interactive" style="height: 300px;"></div>
    </div>
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