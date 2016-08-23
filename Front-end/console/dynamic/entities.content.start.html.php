<div class="row row-centered">
    <div class="col-xs-3 col-centered">
        <button type="submit" class="btn btn-success flat" id="btnCreate" value="" onClick="addEntity()">Create Entity</button>
    </div>
    <div class="col-xs-6 col-centered text-center">
        <h4>Entities</h4>
    </div>

    <div class="col-xs-3 pull-right">
        <a data-toggle="modal"  data-target="#infoEntities" style="cursor: pointer;">
            <div class="pull-right" style="margin-top: 10px;">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>

</div>
<p></p>

<!-- Modal INFO-->
<div class="modal fade" id="infoEntities" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">AI Entities</h4>
            </div>
            <div class="modal-body">
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description Actions</dt>
                        <dd>Tell the AI learning ...</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>