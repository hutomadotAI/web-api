<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"><b>Entities</b></div>
        <a data-toggle="collapse" href="#collapseActionInfo" tabindex="-1">
            <div class="pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body no-margin" id="boxAction" style="padding-top: 0;">
       
        <div id="collapseActionInfo" class="panel-collapse collapse">
            <div class="box-body" style="padding: 10px 0 0 0;">
                <div class="overlay center-block">
                    <section class="content-info">
                        <div class="box-body">
                            <dl class="dl-horizontal no-margin" style="text-align:justify">
                                Entities are the objects that fulfil an intent. Imagine you are creating a bot that takes orders in a bar,  a customer may ask "I would like to order ...."X".
                                X here is an entity you would want the bot to extract from a conversation. These could include "beer", "wine" or "cola" which fall into the drinks category.  You could list further entities under food.
                            </dl>
                        </div>
                    </section>
                </div>
            </div>
        </div>
        
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentVariable" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntentVariable"></i>
            <span id="msgAlertIntentVariable">List all entities that you would like the bot to extract from a conversation.</span>
        </div>

        <div class="box-body flat" style="background-color: #404446;  margin-top: -1px;">
            <div class="row">
                <div class="col-xs-3">
                    <div class="text-center">
                        <i class="fa fa fa-sitemap text-md text-md text-yellow" data-toggle="tooltip"
                           title="This parametere needs to"></i>
                        Entity
                    </div>
                </div>
                <div class="col-xs-3">
                    <div class="text-center">
                        <i class="fa fa-sliders text-md text-red" data-toggle="tooltip"
                           title="This parametere needs to"></i>
                        N°prompts
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="text-center">
                        <i class="fa  fa-comments text-md text-blue" data-toggle="tooltip"
                           title="This parametere needs to"></i>
                        Prompt
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="text-center">
                        Required
                    </div>
                </div>
            </div>
        </div>

        <div class="box-body flat no-padding" style="padding-top: 5px;"id="parameter-list"></div>

        <p></p>
        <button type="button" class="btn btn-primary flat pull-right" id="addParameter" value="">Add Entity</button>

    </div>

    <div class="box-footer">
        <span>
            If you’re stuck check out our <a data-toggle="collapse" href="#collapseVideoTutorialIntent">intents variables tutorial</a> or email <a href='mailto:support@hutoma.com?subject=Invite%20to%20slack%20channel' tabindex="-1">support@hutoma.com</a> for an invite to our slack channel.
        </span>
        <p></p>
    </div>

</div>
