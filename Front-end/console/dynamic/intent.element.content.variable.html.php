<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"><b>Intent Variables</b></div>
        <a data-toggle="collapse" href="#collapseActionInfo" tabindex="-1">
            <div class="pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body no-margin" id="boxAction" style="padding-top: 0;">
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentVariable" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntentVariable"></i>
            <span id="msgAlertIntentVariable">Set the variables used by the intent.</span>
        </div>

        <div id="collapseActionInfo" class="panel-collapse collapse">
            <div class="box-body" style="padding:0 0 10px 0;">
                <div class="overlay center-block">
                    <section class="content-info">
                        <div class="box-body">
                            <dl class="dl-horizontal" style="text-align:justify">
                                Intent variables are entities that you will want the AI to know before it flags you that
                                the intent is fulfilled.
                                For example, when you order coffee you might be asked what kind of coffee you want.
                                The type of coffee would be a variable you want to model here.</dl>
                        </div>
                    </section>
                    <section class="content-info" style="padding-left:15px;">
                        need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                    </section>
                </div>
            </div>
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
                        <i class="fa fa-tag text-md text-blue" data-toggle="tooltip"
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
        <button type="button" class="btn btn-primary flat pull-right" id="addParameter" value="">Add parameter</button>

    </div>

</div>
