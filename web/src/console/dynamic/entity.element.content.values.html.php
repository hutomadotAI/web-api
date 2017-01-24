<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border ">
        <div class="box-title"><span style="padding: 0 3px 0 3px;"><b><?= $entityName ?></b> > </span><b>Values</b></div>
        <a data-toggle="collapse" href="#collapseValuesInfo">
            <div class="pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseValuesInfo" class="panel-collapse collapse">
        <div class="box-body" style="padding-bottom:0;">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal" style="text-align:justify">
                            bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla
                        </dl>
                    </div>
                </section>
                <section class="content-info" style="padding-left:15px;">
                    need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body no-margin" id="boxValues" style="padding-top:0;">

        <div class="row">
            <div class="col-md-12">
                <h5 class="box-title">
                    <div class="input-group no-margin">
                        <input type="text" class="form-control flat no-shadow" id="value-entity" name="value-entity"
                               placeholder="Add value for entity" onkeyup="checkValueCode(this,event.keyCode)"
                               style="width: 96%;">
                        <span class="input-group-btn">
                            <button class="btn btn-success flat" id="btnAddEntityValue" style="width: 130px;">Add Entity Value</button>
                        </span>
                    </div>
                </h5>
            </div>
        </div>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntityValues" style="margin-bottom:10px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertEntityValues"></i>
            <span id="msgAlertEntityValues">You can add additional values for this entity</span>
        </div>

        <div class="row" id="entityValues-list"></div>
    </div>

</div>