<div class="box box-solid box-clean flat no-shadow" >

    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <h3 class="box-title">Intents List</h3>
        <a data-toggle="collapse"  href="#collapseIntentsListInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body">

        <div class="input-group-btn">
            <input class="form-control pull-right" onkeyup="searchIntents(this.value)" value="" placeholder="Search intents...">
        </div>

        <p></p>

        <div id="collapseIntentsListInfo" class="panel-collapse collapse">
            <div class="box-body">
                <div class="overlay center-block">
                    <section class="content bg-gray-light" >
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
                    <p></p>
                    need help? check our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                </div>
            </div>
        </div>

        <div class="tab-pane" id="tab_intents">
            <p id="intentsearch"></p>
        </div>
        <p></p>
    </div>

</div>


<!-- Modal DELETE entity-->
<div class="modal fade" id="deleteIntent" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">DELETE Intent</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="delete-intent-label">
                    
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" id="modalDelete" value="" onClick="deleteIntent(this.value)" data-dismiss="modal">Delete</button>
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>