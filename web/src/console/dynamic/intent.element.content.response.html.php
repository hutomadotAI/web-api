<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"></span><b>Responses</b></div>
        <a data-toggle="collapse" href="#collapseIntentResponse">
            <div class="pull-right">more info
                <i class="fa fa-info-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseIntentResponse" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal no-margin" style="text-align:justify">
                            Intent responses are the pre-packaged answers that you would like your bot to respond when given an intent by a user.
                        </dl>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body no-margin" id="boxIntentResponse"  style="padding-top: 0;">

        <div class="row">
            <div class="col-md-12">
                <h5 class="box-title">
                    <div class="input-group no-margin">
                        <input type="text" class="form-control flat no-shadow" id="intent-response" name="intent-response"
                               placeholder="Type a response here..." onkeyup="checkIntentResponseCode(this,event.keyCode)"
                               style="width: 96%;">
                        <span class="input-group-btn">
                            <button class="btn btn-success flat" id="btnAddIntentResponse" style="width: 130px;" disabled>Add Response</button>
                        </span>
                    </div>
                </h5>
            </div>
        </div>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentResponse" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntentResponse"></i>
            <span id="msgAlertIntentResponse">Give the bot examples of how it should respond to a user’s intent.</span>
        </div>

        <div class="row" id="intentresponse-list"></div>
    </div>

</div>