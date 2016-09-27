 <div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border ">
        <!--<i class="fa fa-comment-o text-green"></i>-->
        <h3 class="box-title"><?php echo $_POST['intent']?><span class="text-sm text-gray" style="padding: 0 3px 0 3px;"> > </span> User Expression</h3>
        <a data-toggle="collapse"  href="#collapseExpressionInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>
     

     <div class="box-body no-margin" id="boxIntent"  style="padding-top: 0;">
            <div class="row">
                <div class="col-md-12">
                    <h5 class="box-title">
                        <div class="input-group no-margin">
                            <input type="text" class="form-control" id="user-expression" name="user-expression" placeholder="Add user expression" onkeyup="checkKeyCode(this,event.keyCode)"  style="width: 96%;">
                            <span class="input-group-btn">
                            <button class="btn btn-success flat pull-right" id="btnAddExpression"  style="width: 120px;" disabled>Add Expression</button>
                            </span>
                        </div>
                    </h5>
                </div>
            </div>

         <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUserExpression">
             <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
             <i class="icon fa fa-check" id="iconAlertUserExpression"></i>
             <span id="msgAlertUserExpression">You can add user expressions and save it</span>
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
                         need help? check our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                     </div>
                 </div>
             </div>

            <div class="row" id="userexpression-list"></div>

         <!--<div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUserExpression">
              <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                 <i class="icon fa fa-check" id="iconAlertUserExpression"></i>
                 <span id="msgAlertUserExpression"></span>
            </div>
     -->
    </div>

</div>