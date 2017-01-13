<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <i class="fa fa-sitemap text-yellow"></i>
        <div class="box-title"><b>Entities List</b></div>
        <a data-toggle="collapse" href="#collapseEntitiesListInfo">
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
            <div class="box-body" style="padding-top:0px;padding-left:0;padding-right:0;">
                <div class="overlay center-block">
                    <section class="content-info" >
                        <div class="box-body">
                            bla bla bla bla bla  bla bla bla bla bla  bla bla bla bla bla  bla bla bla bla bla  bla bla bla bla bla
                        </div>
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
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close text-gray" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">DELETE Entity</h4>
            </div>
            <div class="modal-body" style="background-color: #212121" >
                <div class="box-body" id="delete-entity-label">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger flat" id="modalDelete" value="" onClick="deleteEntity(this.value)" data-dismiss="modal">Delete</button>
            </div>
        </div>

    </div>
</div>




