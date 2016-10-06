 <div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border ">
        <!--<i class="fa fa-comment-o text-green"></i>-->
        <h3 class="box-title"><?php echo $_POST['intent']?><span class="text-sm text-gray" style="padding: 0 3px 0 3px;"> > </span> User Expressions</h3>
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
                            <input type="text" class="form-control" id="user-expression" name="user-expression" placeholder="Add a sample user expression" onkeyup="checkKeyCode(this,event.keyCode)"  style="width: 96%;">
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
             <span id="msgAlertUserExpression">Give the AI examples of how a user would express this intent.</span>

         </div>
         <b>need help?</b> check our <a data-toggle="collapse"  href="#collapseVideoTutorialIntent">Intents tutorial</a> or email <a href='#'>support@hutoma.com</a>

             <div id="collapseExpressionInfo" class="panel-collapse collapse">
                 <div class="box-body">
                     <div class="overlay center-block">
                         <section class="content bg-gray-light" >
                             <div class="box-body">
                                 <dl class="dl-horizontal"  style="text-align:justify">>
                                     User expressions are a collection of examples that help the AI understand how humans typically express an intent. When you order a coffee, for example, you might say 'id like a coffe please' or 'A tall Americano, please'. Those are examples of your Intent to order a coffe.  User expressions are used the AI to detect topics or phrases that might be interesting for you.
                                 </dl>
                             </div>
                         </section>

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