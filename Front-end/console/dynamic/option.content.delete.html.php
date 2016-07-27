<div class="panel box flat no-border">
<div class="box-header flat no-border">
<div class="row">
      <div class="col-xs-1">
      <img src="./dist/img/generic/warning.png" width="25" height="25">
      </div>
      <div class="col-xs-3">
      <span class="lead red">Delete AI</span>
      </div>
      <div class="col-xs-4">
      <span class="text-muted pull-right">Be very careful before continuing</span>
      </div>
   
      <div class="col-xs-4">
      <a data-toggle="collapse" data-parent="#accordion" href="#collapseDelete">
      <button class="btn btn-danger pull-right flat" style="margin-right: 5px;"><i class="fa fa fa-warning"></i> continue...</button>
      </a>
      </div>
      </div>
</div>

<div id="collapseDelete" class="panel-collapse collapse">
<div class="box-body">
      <div class="overlay center-block">
      <section class="invoice bg-yellow disabled color-palette">
            <div class="row">
                  <div class="col-xs-12 center-block text-center">
                  Are you sure you want to delete
                  <label><?php echo strtoupper($_SESSION['current_ai_name']);?>
                  ?  All corresponding data and cannot be undone!!
                  </div>
            </div>
            <br>
            <form method="POST" action="./deleteAI.php" onsubmit="disableButton()">
            <div class="row">
                  <button type="input" id="btnDelete" class="btn btn-block btn-danger tn-lg center-block flat" style="width: 250px">
                        <i class="fa fa fa-warning" style="padding-right:2em"></i>
                        I am absolutely sure!!!
                        <i class="fa fa fa-warning" style="padding-left:2em"></i>
                  </button>
            </div>
            </form>

      </section>
      </div>
</div>
</div>

