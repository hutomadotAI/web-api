<!-- Modal BUY a Bot-->

<div class="modal fade" id="buyBot" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content no-border no-shadow" style="background-color: transparent;">
            <div class="modal-header">
                <div class="col-xs-4 no-padding <?php echo $bot['widgetColor'];?> ">
                    <div class="bot-icon" id="botIcon">
                        <i class="<?php echo $bot['iconPath'];?>" style="padding-top:45px;"></i>
                    </div>
                </div>
            </div>


            <div class="modal-body" style="background-color: #212121" >
                <div class="box-body" id="delete-ai-label">

                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="">
                    <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-danger flat" id="modalDelete" data-dismiss="modal">Delete
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>