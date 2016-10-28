<div class="box box-solid box-clean flat no-shadow" >

    <div class="box-header with-border">
        <i class="fa fa-sitemap text-yellow"></i>
        <div class="box-title"><b>Entities List</b></div>
        <a data-toggle="collapse"  href="#collapseEntitiesListInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body">

        <div class="input-group-btn">
            <input class="form-control flat no-shadow pull-right" onkeyup="searchEntities(this.value)" value="" placeholder="Search entities...">
        </div>
    
        <p></p>

        <div id="collapseEntitiesListInfo" class="panel-collapse collapse">
            <div class="box-body" style="padding-top:0;">
                <div class="overlay center-block">
                    <section class="content-info" >
                        <div class="box-body">
                            <dl class="dl-horizontal">
                                <dt>Description Entities Manipulation</dt>
                                <dd>Before start training process, y.</dd>
                                <dt>Euismod</dt>
                                <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                                <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                                <dt>Malesuada porta</dt>
                                <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                                <dt>Felis euismod semper eget lacinia</dt>
                                <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                            </dl>
                        </div>
                    </section>
                    <section class="content-info" >
                        <dd> need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a></dd>
                    </section>
                </div>
            </div>
        </div>

        <div class="tab-pane" id="tab_entities">
            <p id="entsearch"></p>
        </div>
        <p></p>
    </div>

</div>


<!-- Modal DELETE entity-->
<div class="modal fade" id="deleteEntity" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content padding" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">DELETE Entity</h4>
            </div>
            <div class="modal-body" style="background-color: #515151" >
                <div class="box-body" id="delete-entity-label">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger flat" id="modalDelete" value="" onClick="deleteEntity(this.value)" data-dismiss="modal">Delete</button>
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>




