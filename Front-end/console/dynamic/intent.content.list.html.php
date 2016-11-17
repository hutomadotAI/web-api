<div class="box box-solid box-clean flat no-shadow">

    <div class="box-header with-border">
        <i class="fa fa-commenting-o text-green"></i>
        <div class="box-title"><b>Intents List</b></div>
        <a data-toggle="collapse" href="#collapseIntentsListInfo">
            <div class=" pull-right">more info
                <i class="fa fa-info-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body">

        <div class="input-group-btn">
            <input class="form-control flat no-shadow pull-right" onkeyup="searchIntents(this.value)" value="" placeholder="Search intents...">
        </div>

        <p></p>

        <div id="collapseIntentsListInfo" class="panel-collapse collapse">
            <div class="box-body" style="padding-top:0px;padding-left:0;padding-right:0;">
                <div class="overlay center-block">
                    <section class="content-info">
                        <div class="box-body">
                            This is the list of all available intents that can be attached to your AI.
                        </div>
                    </section>
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
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close text-gray" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Delete Intent</h4>
            </div>
            <div class="modal-body" style="background-color: #212121" >
                <div class="box-body" id="delete-intent-label">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger flat" id="modalDelete" value=""
                        onClick="deleteIntent(this.value)" data-dismiss="modal">Delete
                </button>
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>