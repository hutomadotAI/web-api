<div class="box box-solid box-success flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <i class="fa fa-certificate"></i>
        <h3 class="box-title">Settings User Contrat</h3>
    </div>
    <form method="POST" id="domainsNweAIform" action="./trainingAI.php" onsubmit="RecursiveUnbind($('#wrapper'));">

        <div class="input-group-btn">
            <input type="hidden" id="userActivedDomains"name="userActivedDomains" value="<?php echo $_SESSION['userActivedDomains'] ?>">
        </div>

        <div class="box-body">
            <div class="row">

                <!-- INPUT Name -->
                <div class="col-md-6">
                    <div class="form-group">
                        <a data-toggle="collapse"  href="#collapseContracts">
                            <div class=" pull-right">more info
                                <i class="fa fa-question-circle text-md text-yellow"></i>
                            </div>
                        </a>
                        <label>Select type of contract for your end user</label>
                        <select class="form-control select2" name="ai_language" id="ai_language"style="width: 100%;">
                            <option selected="selected">trial</option>
                            <option>license</option>
                            <option>perpetual</option>
                        </select>
                    </div>

                    <div id="collapseContracts" class="panel-collapse collapse">
                        <div class="box-body">
                            <div class="overlay center-block">
                                <section class="content bg-gray-light" >
                                    <div class="box-body">
                                        <dl class="dl-horizontal">
                                            <dt>Description Actions</dt>
                                            <dd>Tell the AI learning ...</dd>
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


                <!-- INPUT Language -->
                <div class="col-md-6">
                    <div class="form-group">
                        <div class="form-group">
                            <a data-toggle="collapse"  href="#collapsePayments">
                                <div class=" pull-right">more info
                                    <i class="fa fa-question-circle text-md text-yellow"></i>
                                </div>
                            </a>
                            <label>Select type of contract for your end user</label>
                            <select class="form-control select2" name="ai_language" id="ai_language"style="width: 100%;">
                                <option>personal</option>
                                <option selected="selected">Monthly fee</option>
                                <option>Year fee</option>
                            </select>
                        </div>

                    </div>

                    <div id="collapsePayments" class="panel-collapse collapse">
                        <div class="box-body">
                            <div class="overlay center-block">
                                <section class="content bg-gray-light" >
                                    <div class="box-body">
                                        <dl class="dl-horizontal">
                                            <dt>Description Actions</dt>
                                            <dd>Tell the AI learning ...</dd>
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
            </div>




        </div>
    

        <div class="box-footer">
                <button type="submit" class="btn btn-success flat" id="btnSave" value="" onClick="">save</button>
        </div>
    </form>
</div>


<!-- Modal INFO Type of plans-->
<div class="modal fade" id="infoPlans" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Type of plans</h4>
            </div>
            <div class="modal-body">
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description Actions</dt>
                        <dd>Tell the AI learning ...</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>


<!-- Modal INFO Type of contract-->
<div class="modal fade" id="infoContract" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Type of COntract</h4>
            </div>
            <div class="modal-body">
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description Actions</dt>
                        <dd>Tell the AI learning ...</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>
