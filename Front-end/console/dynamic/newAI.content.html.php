<div class="box box-solid box-clean flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <i class="fa fa-user-plus text-blue"></i>
        <h3 class="box-title">Create Your New AI</h3>
    </div>
    
    <form method="POST" name="createAIform" action="./domainsNewAI.php">
        <div class="box-body">
            <div class="row">

                <!-- INPUT Name -->
                <div class="col-md-6">
                    <div class="form-group">
                        <label>Name</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                            <i class="glyphicon glyphicon-user"></i>
                            </div>
                            <input type="text" class="form-control" id="ai_name" name="ai_name" placeholder="Enter your AI name">
                        </div>
                    </div>

                    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertNameAI" style="display:none;">
                        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                        <i class="icon fa fa-check" id="iconAlertNameAI"></i>
                        <span id="msgAlertNameAI"></span>
                    </div>

                </div>

                <!-- INPUT Language -->
                <div class="col-md-6">
                     <?php include 'input.language.html.php'; ?>
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
                      <input type="text" class="form-control" name="ai_description" id="ai_description" placeholder="Enter your AI description">
                  </div>
                  </div>
                </div>

                <!-- INPUT Time Zone -->
                <div class="col-md-6">
                    <?php include './dynamic/input.timezone.html.php'; ?>
                </div>
            </div>

            <div class="row">

                <!-- Create a new Answer -->
                <div class="col-md-6">
                    <a data-toggle="collapse"  href="#collapseLearn" tabindex="-1">
                        <div class=" pull-right">more info
                            <i class="fa fa-question-circle text-md text-yellow"></i>
                        </div>
                    </a>
                    <label>Create new Answers</label>
                    <div class="box box-solid box-clean-fixed flat no-shadow">
                    <div class="box-body">
                    <div class="row margin">
                        <div class="col-sm-12">
                            <input type="hidden" name="ai_confidence" id="ai_confidence" value="0;10">
                        </div>
                    </div>
                    </div>
                    </div>
                </div>

                <!-- INPUT Voice -->
                <div class="col-md-6">
                    <?php include './dynamic/input.personality.html.php'; ?>
                    <div class="form-group">
                        <label>Voice</label>
                        <select class="form-control select2" name="ai_voice" id="ai_voice" style="width: 100%;">
                            <option selected="selected" id="_male">Male</option>
                            <option id="_female">Female</option>
                        </select>
                    </div>
                </div>

            </div>

            <!-- INPUT Public -->
            <div class="row">
                <div class="col-md-12">
                    <div class="form-group pull-right no-margin" style="padding-top: 5px;">
                        <span style="padding-right:5px;">Make this AI public</span>
                        <label>
                            <input type="checkbox" name="ai_public" id="ai_public" class="flat-red" checked>
                        </label>
                    </div>
                </div>
            </div>

    </form>

        <div class="row">

            <div class="col-md-6">
                <div id="collapseLearn" class="panel-collapse collapse">
                    <div class="box-body">
                        <div class="overlay center-block">
                            <section class="content bg-gray-light" >
                                <div class="box-body">
                                        <dt>What does this mean?</dt>
                                        <dd>By enabling this functionality you will let the AI decide when to follow pre-pacakged answers vs creating new ones based on the trainig data you provide.</dd>
                                </div>
                            </section>
                            <p></p>
                            need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>
                        </div>
                    </div>
                </div>
            </div>


            <div class="col-md-6">
                <div id="collapsePersonality" class="panel-collapse collapse">
                    <div class="box-body">
                        <div class="overlay center-block">
                            <section class="content bg-gray-light" >
                                <div class="box-body">
                                    <dt>NOT YET IMPLEMENTED</dt>
                                </div>
                            </section>
                            <p></p>
                            need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>
                        </div>
                    </div>
                </div>
            </div>

        </div>


       
    </div>


    <div class="box-footer">
            <a href="#" class="btn btn-primary flat" id="btnCancel" onClick="history.go(-1); return false;">cancel</a>
            <button type="submit" id="btnNext" class="btn btn-success flat" alt="next step">next</button>
    </div>
</div>

