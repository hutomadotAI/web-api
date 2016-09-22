<div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border">
        <i class="fa fa-sitemap text-yellow"></i>
        <h3 class="box-title">New Entity</h3>
        <a data-toggle="collapse"  href="#collapseEntitiesInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>


    <div class="box-body" id="boxEntities">
        <div class="bootstrap-filestyle input-group" id="GrpEntityButton">
            <form method="POST" name="entityCreateForm" action="./entityelement.php" onsubmit="RecursiveUnbind($('#wrapper'));">
                <input type="text" class="form-control" id="inputEntityName" name="entity" placeholder="Enter entity name" style="width: 96%;">
            </form>
            <div class="input-group-btn" tabindex="0">
                <button id="btnCreateEntity"  class="btn btn-success flat" style="width: 120px;" alt="create entity" disabled> Create Entity</button>
            </div>
        </div>
        <p></p>
        
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntity">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertEntity"></i>
            <span id="msgAlertEntity"></span>
        </div>
    </div>



    <div id="collapseEntitiesInfo" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content bg-gray-light" >
                    <div class="box-body">
                        <dl class="dl-horizontal">
                            <dt>Description Entities Manipulation</dt>
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


