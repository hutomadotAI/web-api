<!-- Modal BUY a Bot-->


<div class="modal fade" id="buyBot" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa fa-warning text-danger" style="padding-right:2em"></i> DELETE AI
                </h4>
            </div>
            <div class="modal-body" style="background-color: #212121" >
                <div class="box-body" id="delete-ai-label">

                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="./dynamic/deleteai.php">
                    <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-danger flat" id="modalDelete" data-dismiss="modal">Delete
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>