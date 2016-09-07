<div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <h3 class="box-title">Intents</h3>
        <a data-toggle="collapse"  href="#collapseIntentsInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>


    <div class="box-body" id="boxIntents">
        <div class="bootstrap-filestyle input-group" id="GrpIntentButton">
            <form method="POST" name="intentCreateForm" action="./intentelement.php" onsubmit="RecursiveUnbind($('#wrapper'));">
            <input type="text" class="form-control" id="inputIntentName" name="intent" placeholder="Enter intent name" style="width: 96%;">
            </form>
            <div class="input-group-btn" tabindex="0">
                    <button id="btnCreateIntent"  class="btn btn-success flat" style="width: 120px;" alt="create intent" disabled>Create Intent</button>
            </div>

        </div>
        <p></p>
        
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntent">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertIntent"></i>
            <span id="msgAlertIntent"></span>
        </div>
    </div>



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


