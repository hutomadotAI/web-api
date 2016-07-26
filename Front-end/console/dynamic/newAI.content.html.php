<div class="box box-solid box-success flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
    <h3 class="box-title">Create Your New AI</h3>
    </div>
    <form method="POST" id="createAIform" action="./domainsNewAI.php">
    <div class="box-body">
        <div class="row">
              <!-- Input Name -->
              <div class="col-md-6">
              <div class="form-group">
              <label for="ainame">Name</label>
              <div class="input-group">
                  <div class="input-group-addon">
                  <i class="glyphicon glyphicon-user"></i>
                  </div>
              <input type="text" class="form-control" name="ai_name" id="ai_name" placeholder="Enter your AI name" onkeyup="activeNext(this.value)">
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
                  <input type="text" class="form-control" name="ai_description" id="ai_description" placeholder="insert description">
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
                    <label><input type="radio" name="ai_type" value="public" checked>AI Public</label>
                    </div>
                    <p></p>
                    <div class="radio">
                    <label><input type="radio" name="ai_type" onmouseover="mouseOver('doorMouseover')" onmouseout="mouseOff('doorMouseover')" value="private" disabled>AI Private</label>
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
                        <input id="confidence" type="hidden" name="ai_confidence" id_confidence="ai_confidence" value="0;10">
                    </div>
                    </div>
              </div>      
              </div>
              </div>
         </div>
    </div>
    </form>

    <div class="box-footer">
        <a href="newAI.php" class="btn btn-primary flat" id="btnCancel">cancel</a>
        <button type="submit" id="btnNext"  class="btn btn-success flat disabled" onClick="" alt="next step">next</button>
    </div>
</div>
