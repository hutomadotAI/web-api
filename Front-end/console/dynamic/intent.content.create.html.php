<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <div class="box-title"><b>New Intent</b></div>
        <a data-toggle="collapse"  href="#collapseIntentsInfo">
            <div class=" pull-right"><i class="fa fa-info-circle text-sm text-yellow"></i> more info

            </div>
        </a>
    </div>

    <div class="box-body" id="boxIntents">
        <div class="bootstrap-filestyle input-group" id="GrpIntentButton">
            <form method="POST" name="intentCreateForm" id="intentCreateForm" action="./intentelement.php">
                <input type="text" class="form-control flat no-shadow" id="inputIntentName" name="intent" placeholder="Intent name" style="width: 96%;">
            </form>
            <div class="input-group-btn" tabindex="0">
                <button id="btnCreateIntent"  class="btn btn-success flat" style="width: 120px;" disabled>Create Intent</button>
            </div>
        </div>
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntent">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertIntent"></i>
            <span id="msgAlertIntent"></span>
        </div>

        <b>need help?</b> check our <a data-toggle="collapse"  href="#collapseVideoTutorialIntent">Intents tutorial</a> or email <a href='#'>support@hutoma.com</a>
        <p></p>
    </div>

    <div id="collapseVideoTutorialIntent" class="panel-collapse collapse">
        <div class="box-body flat">
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

    <div id="collapseIntentsInfo" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        Intents are useful when you want to add your own business logic during a conversation betwen an end user and an AI. Check our video tutorial for more info.
                </section>

            </div>
        </div>
    </div>

</div>


