<div class="input-group-btn">
    <input class="form-control input-lg pull-right" onkeyup="searchEntities(this.value)" value="" placeholder="Search entities...">
</div>
<h2></h2>
<p></p>

<div class="tab-pane" id="tab_entities">
    <p id="entsearch"></p>
</div>
<p></p>


<!-- Modal DELETE entity-->
<div class="modal fade" id="deleteEntity" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">DELETE Entity</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="delete-entity-label">
                    
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" id="modalDelete" value="" onClick="deleteEntity(this.value)" data-dismiss="modal">Delete</button>
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>