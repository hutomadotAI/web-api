<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"></span><b>WebHook</b></div>
        <a data-toggle="collapse" href="#collapseWebHook">
            <div class="pull-right">more info
                <i class="fa fa-info-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseWebHook" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal no-margin" style="text-align:justify">
                            A webhook is a way for an app to provide other applications with real-time information. A webhook delivers data to other applications as it happens, meaning you get data immediately.
                        </dl>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body no-margin" id="boxWebHook" style="padding-top: 0;">

        <div class="row">
            <div class="col-md-12">
                <h5 class="box-title">
                    <div class="input-group-addon no-padding no-border">
                        <div class="col-lg-12 no-margin no-padding">
                            <input type="text" class="form-control flat no-shadow" id="webhook" name="webhook" placeholder="Type endpoint address..." onkeydown="enableSaving(true);">
                        </div>
                    </div>
                </h5>
            </div>
        </div>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertWebHook" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertWebHook"></i>
            <span id="msgAlertWebHook">Provide the WebHook endpoint.</span>
        </div>
    </div>
</div>