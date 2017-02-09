<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <div class="box-title"><b>New Intent</b></div>
        <a data-toggle="collapse"  href="#collapseIntentsInfo">
            <div class=" pull-right">more info
                <i class="fa fa-info-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseIntentsInfo" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal no-margin" style="text-align:justify">
                            Let’s say you’re creating a Bot that takes orders in a bar.  A user may say "I want to order" - this is their intent. The associated entities could be  "beer" "cola"  or “wine".
                        </dl>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body" id="boxIntents">
        <div class="bootstrap-filestyle input-group" id="GrpIntentButton">
            <input type="text" class="form-control flat no-shadow" id="inputIntentName" name="intent" placeholder="Give the intent a name" style="width: 96%;" onkeyup="checkIntentCode(this,event.keyCode)">
            <div class="input-group-btn" tabindex="0">
                <button id="btnCreateIntent"  class="btn btn-success flat" style="width: 120px;">Create Intent</button>
            </div>
        </div>
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntent" style="margin: 0 0 10px 0;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntent"></i>
            <span id="msgAlertIntent"></span>
        </div>
    </div>

    <div class="box-footer">
        <span>
            If you’re stuck check out our <a data-toggle="collapse" href="#collapseVideoTutorialIntent">Intents tutorial</a> or email <a href='#' tabindex="-1">support@hutoma.com</a> for an invite to our slack channel.
        </span>
        <p></p>

        <div id="collapseVideoTutorialIntent" class="panel-collapse collapse">
            <div class="box-body flat no-padding">
                <div class="overlay center-block">
                    <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                        <iframe
                            src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                            frameborder="0" allowfullscreen>
                        </iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>


