<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"></span><b>User Expressions</b></div>
        <a data-toggle="collapse" href="#collapseExpressionInfo">
            <div class="pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseExpressionInfo" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal no-margin" style="text-align:justify">
                            User expressions are a collection of examples that help the AI understand how humans typically express an intent. When you order a coffee, for example, you might say 'id like a coffe please' or 'A tall Americano, please'. Those are examples of your Intent to order a coffe.  User expressions are used the AI to detect topics or phrases that might be interesting for you.
                        </dl>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body no-margin" id="boxExpression" style="padding-top:0;">

        <div class="row">
            <div class="col-md-12">
                <h5 class="box-title">
                    <div class="input-group no-margin">
                        <input type="text" class="form-control flat no-shadow" id="user-expression" name="user-expression"
                               placeholder="Add a sample user expression" onkeyup="checkExpressionCode(this,event.keyCode)"
                               style="width: 96%;">
                        <span class="input-group-btn">
                            <button class="btn btn-success flat" id="btnAddExpression" style="width: 130px;" disabled>Add Expression</button>
                        </span>
                    </div>
                </h5>
            </div>
        </div>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUserExpression" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertUserExpression"></i>
            <span id="msgAlertUserExpression">Give the AI examples of how a user would express this intent.</span>
        </div>

        <div class="row" id="userexpression-list"></div>
     </div>

</div>