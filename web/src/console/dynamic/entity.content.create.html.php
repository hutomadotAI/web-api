<div class="box box-solid box-clean flat no-shadow unselectable" >
   
    <div class="box-header with-border">
        <i class="fa fa-sitemap text-yellow"></i>
        <div class="box-title"><b>New Entity</b></div>
        <a data-toggle="collapse"  href="#collapseEntitiesInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseEntitiesInfo" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0px;">
            <div class="overlay center-block">
                <section class="content-info" >
                    <div class="box-body">
                        <dd>
                            Entities are objects that might be required to fulfil an intent.  Imagine you are creating a Bot that takes orders in a bar,  a customer may ask "I would like to order ...."X".
                            X here is an entity you would want the Bot to extract from a conversation. These could include "beer", "wine" or "cola" which fall into the drinks category.  You could list further entities under food.
                        </dd>
                    </div>
                </section>
            </div>
        </div>
    </div>
    
    <div class="box-body" id="boxEntities">
        <div class="bootstrap-filestyle input-group" id="GrpEntityButton">
            <input type="text" class="form-control flat no-shadow" id="inputEntityName" name="entity" placeholder="Give the entity a name" style="width: 96%;" onkeyup="checkEntityCode(this,event.keyCode)">
            <div class="input-group-btn" tabindex="0">
                <button id="btnCreateEntity"  class="btn btn-success flat" style="width: 120px;"> Create Entity</button>
            </div>
        </div>
        <p></p>
        
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntity" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertEntity"></i>
            <span id="msgAlertEntity">In this section you can create different entities.</span>
        </div>
    </div>

    <div class="box-footer"
        <span>
            If you’re stuck check out our <a data-toggle="collapse" href="#collapseVideoTutorialEntity">entities tutorial</a> or email <a href='mailto:support@hutoma.ai?subject=Invite%20to%20slack%20channel' tabindex="-1">support@hutoma.ai</a> for an invite to our slack channel.
        </span>
        <p></p>


        <div id="collapseVideoTutorialEntity" class="panel-collapse collapse">
            <div class="box-body flat no-padding">
                <div class="overlay center-block">
                    <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                        <iframe
                            src="//www.youtube.com/embed/SI5XgQm660A?controls=1&hd=1&enablejsapi=1"
                            frameborder="0" allowfullscreen>
                        </iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


