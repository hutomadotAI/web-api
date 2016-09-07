 <div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <h3 class="box-title"><?php echo $_POST['intent']?></h3>
        <a data-toggle="collapse"  href="#collapseIntentsInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>


    <div class="box-body" id="boxIntent">

            <form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">
            <div class="input-group-btn" tabindex="0">
                    <button id="btnSaveIntent"  class="btn btn-success flat" style="width: 120px;" alt="save intent" disabled>Save Intent</button>
            </div>
            </form>

            <div class="row">
                <h4 class="box-title">
                <div class="col-md-12">
                    </i><span class="lead"> User expression</span>
                    <p></p>
                    <h5 class="box-title">
                    <div class="inner-addon left-addon">
                        <i class="fa fa-quote-right text-light-blue"></i>
                        <input type="text" class="form-control" id="user-says" name="user-says" placeholder="User expression" style="padding-left: 35px;">
                    </div>
                        </h5>
                </div>
                </h4>
            </div>

            <p></p>
            <p></p>

            <div class="row">
                <h4 class="box-title">
                    <div class="col-md-12">
                        </i><span class="lead"> Action</span>
                        <p></p>
                        <h5 class="box-title">
                            <div class="inner-addon left-addon">
                                <i class="fa fa-wrench text-success"></i>
                                <input type="text" class="form-control" id="action-reaction" name="action-reaction" placeholder="Enter action name" style="padding-left: 35px;">
                            </div>
                        </h5>
                    </div>
                </h4>
            </div>



            <div class="row">
                <h4 class="box-title">
                    <div class="col-md-12">
                        <span class="lead"> Action</span>
                        <p></p>
                        <div class="inner-addon left-addon">
                            <i class="fa fa-comments-o text-muted"></i>
                            <input type="text" class="form-control" id="action-reaction" name="action-reaction" placeholder="Enter action name">
                        </div>
                    </div>
                </h4>
            </div>
            <p></p>
        <p></p>




            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertNameAI" style="display:none;">
                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                <i class="icon fa fa-check" id="iconAlertNameAI"></i>
                <span id="msgAlertNameAI"></span>
            </div>


    </div>
        <p></p>




    <div id="collapseIntentsInfo" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content bg-gray-light" >
                    <div class="box-body">
                        <dl class="dl-horizontal">
                            <dt>Description Intents Manipulation</dt>
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


