<form role="form">
<div class="box-body">
    <div class="row">
        <!-- INPUT Name -->
        <div class="col-md-6">
        <div class="form-group">
        <label for="ainame">Name</label>
        <div class="input-group">
            <div class="input-group-addon">
            <i class="glyphicon glyphicon-user"></i>
            </div>
            <?php echo('<input type="text" class="form-control" name="ai_name" id="ai_name" placeholder="'.$_SESSION['ai_name'].'"disabled>');?>
        </div>
        </div>
        </div>

        <!-- INPUT Language -->
        <div class="col-md-6">
            <?php include 'language.input.html.php'; ?>
        </div>   
    </div>

    <div class="row">
          <!-- INPUT Description -->
          <div class="col-md-6">
          <div class="form-group">
          <label for="aidesciption">Description</label>
          <div class="input-group">
              <div class="input-group-addon">
              <i class="glyphicon glyphicon-pencil"></i>
              </div>
              <?php echo('<input type="text" class="form-control" name="ai_description" id="ai_description" placeholder="'.$_SESSION['ai_description'].'">'); ?>
          </div>
          </div>
          </div>
          <!-- INPUT Time Zone -->
          <div class="col-md-6">
          <?php include './dynamic/timezone.input.html.php'; ?>
          </div> 
    </div>

   
    <div class="row">
       <!-- INPUT Type of AI Zone -->
        <div class="col-md-6">
        <label class="text-center">Set type of AI</label>
        <div class="box box-solid box-clean-fixed flat no-shadow">
        <div class="box-body">
        <div class="form-group">
              <div class="radio">
              <label><input type="radio" name="publicRadio" id="publicRadio" value="public" checked>AI Public</label>
              </div>
              <p></p>
              <div class="radio">
              <label><input type="radio" name="privteRadio" id="privateRadio" value="private" disabled>AI Private</label>
              </div>
        </div>
        </div>
        </div>
        </div>
        <!-- Training option  -->
        <div class="col-md-6 text-left">
        <label class="text-center">Set confidence value</label>
        <div class="box box-solid box-clean-fixed flat no-shadow">
        <div class="box-body">
              <div class="row margin">
              <div class="col-sm-12">
                  <input id="confidence" type="hidden" name="confidence" value="0;10">
              </div>
              </div>
        </div>      
        </div>
        </div>
    </div>

    <h3><p class="text-muted">API key</p></h3>
    <div class="row">
        <div class="col-md-6">
        <div class="input-group">
              <span class="input-group-addon">Client key</i></span>
              <input type="text" class="form-control" placeholder="<?php echo($_SESSION['dev_id']);?>" disabled>
              <span class="input-group-addon" data-toggle="tooltip" title="copy to clipboard" onclick="copyToClipboard('result')"><i class="fa fa-clipboard"></i></span>
        </div>
        </div>
    
        <div class="col-md-6">
        <div class="input-group">
              <span class="input-group-addon">Developer key</i></span>
              <input type="text" class="form-control" placeholder=" <?php echo(\hutoma\console::getDevToken());?>" disabled>
              <span class="input-group-addon" data-toggle="tooltip" title="copy to clipboard" onclick="copyToClipboard('result')" ><i class="fa fa-clipboard"></i></span>
        </div>
        </div>
    </div>
</div> 
</form>

<div class="box-footer">
      <a href="optionAI.php" class="btn btn-primary flat">cancel</a>
      <button  name="btnSave" value="btnSave" id="btnSave" value="_next" class="btn btn-success flat disabled" onClick="" alt="save">save</button>
</div> 