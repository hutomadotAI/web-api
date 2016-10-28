<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border ">
        <h3 class="box-title"><?= $entityName ?><span class="text-sm text-gray" style="padding: 0 3px 0 3px;"> > </span>
            Values</h3>
        <a data-toggle="collapse" href="#collapseValuesInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sx text-yellow"></i>
            </div>
        </a>
    </div>

    <div id="collapseValuesInfo" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content-info">
                    <div class="box-body">
                        <dl class="dl-horizontal" style="text-align:justify">
                            bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla
                        </dl>
                    </div>
                </section>
                <section class="content-info">
                    need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                </section>
            </div>
        </div>
    </div>

    <div class="box-body no-margin" id="boxValues">

        <div class="row">
            <div class="col-md-12">
                <h5 class="box-title">
                    <div class="inner-addon left-addon">
                        <i class="fa fa-sign-out text-yellow" style="padding-top:11px;"></i>
                        <input type="text" class="form-control flat no-shadow" id="value-entity" name="value-entity"
                               placeholder="add an entity value" onkeydown="checkValueCode(this,event.keyCode)"
                               style="padding-left: 35px;">
                    </div>
                </h5>
            </div>
        </div>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertEntityValues">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertEntityValues"></i>
            <span id="msgAlertEntityValues">You can add additional values for this entity</span>
        </div>

        <div class="row" id="entityValues-list"></div>
    </div>

</div>