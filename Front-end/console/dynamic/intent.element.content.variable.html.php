<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border">
        <!--<i class="fa fa-wrench text-success"></i>-->
        <div class="box-title"><b>Intent Variables</b></div>
        <a data-toggle="collapse" href="#collapseActionInfo" tabindex="-1">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body no-margin" id="boxAction" style="padding-top: 0;">
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentVariable">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntentVariable"></i>
            <span id="msgAlertIntentVariable">Set the variables used by the intent.</span>
        </div>


        <div id="collapseActionInfo" class="panel-collapse collapse">
            <div class="box-body">
                <div class="overlay center-block">
                    <section class="content-info">
                        <div class="box-body">
                            <dl class="dl-horizontal" style="text-align:justify">
                                Intent variables are entities that you will want the AI to know before it flags you that
                                the intent is fulfilled.
                                For example, when you order coffee you might be asked what kind of coffee you want.
                                The type of coffee would be a variable you want to model here.
                            </dl>
                        </div>
                    </section>

                </div>
            </div>
        </div>

        <div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px; padding-bottom:3px;">
            <div class="row">
                <div class="col-xs-3 border-right">
                    <div class="text-center">
                        <i class="fa fa fa-sitemap text-md text-md text-yellow" data-toggle="tooltip"
                           title="This parametere needs to"></i>
                        Entity
                    </div>
                </div>
                <div class="col-xs-3 border-right">
                    <div class="text-center">
                        <i class="fa fa-sliders text-md text-red" data-toggle="tooltip"
                           title="This parametere needs to"></i>
                        N°prompts
                    </div>
                </div>
                <div class="col-xs-4 border-right">
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

        <div class="box-body bg-white flat no-padding" id="parameter-list"></div>

        <p></p>
        <button type="button" class="btn btn-primary flat pull-left" id="addParameter" value="">Add parameter</button>

    </div>

</div>
