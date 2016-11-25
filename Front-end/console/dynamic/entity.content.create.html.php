<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border">
        <i class="fa fa-sitemap text-yellow"></i>
        <div class="box-title"><b>New Entity</b></div>
        <a data-toggle="collapse" href="#collapseEntitiesInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="boxEntities">
        <div class="bootstrap-filestyle input-group" id="GrpEntityButton">
            <input type="text" class="form-control flat no-shadow" id="inputEntityName" name="entity"
                   placeholder="Enter entity name" style="width: 96%;" onkeyup="checkEntityCode(this,event.keyCode)">
            <div class="input-group-btn" tabindex="0">
                <button id="btnCreateEntity" class="btn btn-success flat" style="width: 120px;" disabled> Create
                    Entity
                </button>
            </div>
        </div>
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntity">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertEntity"></i>
            <span id="msgAlertEntity">In this section you can create different entities.</span>
        </div>

        <b>need help?</b> check our <a data-toggle="collapse" href="#collapseVideoTutorialEntity">Entities tutorial</a>
        or email <a href='#'>support@hutoma.com</a>
        <p></p>
    </div>

    <div id="collapseVideoTutorialEntity" class="panel-collapse collapse">
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

    <div id="collapseEntitiesInfo" class="panel-collapse collapse">
        <div class="box-body" style="padding-top:0px;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla
                        bla
                    </div>
                </section>
            </div>
        </div>
    </div>

</div>


