 <div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <h3 class="box-title">Intent</h3>
        <a data-toggle="collapse"  href="#collapseExpressionInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>


     <div class="box-header bg-gray-light">

         <div class="bootstrap-filestyle input-group" id="GrpIntentButton">
             <form method="POST" name="intentCreateForm" onsubmit="RecursiveUnbind($('#wrapper'));">
                 <input type="text" class="form-control" id="inputIntentName" name="intent" placeholder="Enter intent name" value="<?php echo $_POST['intent']?>" style="width: 96%;" disabled>
             </form>
             <div class="input-group-btn" tabindex="0">
                 <button id="btnSaveIntent"  class="btn btn-success flat" style="width: 120px;" alt="save intent" disabled>Save Intent</button>
             </div>
         </div>
     </div>




     <div class="box-body" id="boxIntent">

            <div class="row">
                <h4 class="box-title">
                <div class="col-md-12">
                    </i><span class="lead"> User expression</span>
                    <p></p>
                    <h5 class="box-title">
                    <div class="inner-addon left-addon">
                        <i class="fa fa-quote-right text-light-blue"></i>
                        <input type="text" class="form-control" id="user-expression" name="user-expression" placeholder="User expression" onkeydown="checkKeyCode(this,event.keyCode)"  style="padding-left: 35px;">
                    </div>
                    </h5>
                </div>
                </h4>
            </div>

            <div class="row" id="userexpression-list"></div>

         <!--<div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUserExpression">
              <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                 <i class="icon fa fa-check" id="iconAlertUserExpression"></i>
                 <span id="msgAlertUserExpression"></span>
            </div>
     -->
    </div>

     <div id="collapseExpressionInfo" class="panel-collapse collapse">
         <div class="box-body">
             <div class="overlay center-block">
                 <section class="content bg-gray-light" >
                     <div class="box-body">
                         <dl class="dl-horizontal">
                             <dt>Description Expression Manipulation</dt>
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