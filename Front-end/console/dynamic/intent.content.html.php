<div class="input-group-btn">
    <input class="form-control input-lg pull-right" onkeyup="searchIntents(this.value)" value="" placeholder="Search intents...">
</div>
<h2></h2>
<p></p>

<div class="tab-pane" id="tab_intents">
    <p id="intentsearch"></p>
</div>
<p></p>


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