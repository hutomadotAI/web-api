<div class="box box-solid box-clean flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <i class="fa fa-certificate text-yellow"></i>
        <h3 class="box-title">Settings User Contrat</h3>
        <a data-toggle="collapse"  href="#collapsePayments">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>
    <form method="POST" id="domainsNweAIform" action="./trainingAI.php" onsubmit="RecursiveUnbind($('#wrapper'));">

        <div class="input-group-btn">
            <input type="hidden" id="userActivedDomains"name="userActivedDomains" value="<?php echo $_SESSION['userActivedDomains'] ?>">
        </div>

        <div class="box-body">
            <div class="row">

                <div class="col-md-4">
                    <div class="form-group">
                        <label>Select type of contract for your end user</label>
                        <select class="form-control select2" name="ai_contract" id="ai_contract"style="width: 100%;">
                            <option selected="selected">trial</option>
                            <option>license</option>
                            <option>perpetual</option>
                        </select>
                    </div>
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
                                    </dl>
                                </div>
                            </section>
                            <p></p>
                            need help? email us <a href='#'>hello@hutoma.com</a>
                        </div>
                    </div>
                </div>



                <div class="col-md-4">
                    <div class="form-group">
                        <div class="form-group">
                            <label>Select type of contract for your end user</label>
                            <select class="form-control select2" name="ai_payment_type" id="ai_payment_type"style="width: 100%;">
                                <option>personal</option>
                                <option selected="selected">Monthly fee</option>
                                <option>Year fee</option>
                            </select>
                        </div>
                    </div>
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
                                    </dl>
                                </div>
                            </section>
                            <p></p>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="form-group">
                        <label for="ainame">Price</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-euro"></i>
                            </div>
                            <input type="text" class="form-control" name="ai_name" id="ai_name" placeholder="Enter your AI name" onkeyup="activeNext(this.value)" class="form-control" data-inputmask='"mask": "0-9.0-9"' data-mask>
                        </div>
                    </div>
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
                                    </dl>
                                </div>
                            </section>
                            <p></p>
                        </div>
                    </div>
                </div>







            </div>




        </div>

        <div id="collapseStart" class="panel-collapse collapse">
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
