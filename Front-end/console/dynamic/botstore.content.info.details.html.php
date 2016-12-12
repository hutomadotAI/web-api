<!-- Modal -->
<div class="modal fade" id="boxBotStoreInfo" role="dialog">
    <div class="modal-dialog flat">
        <div class="modal-content bordered" style="background-color: #202020">

            <div class="modal-header">
                <button type="button" class="close text-gray" id="btnModelClose" data-dismiss="modal">&times;</button>
                <div class="modal-title">
                    <span name="curr_bot_name" value=""></span>
                </div>
                <div class="box-header with-border">
                    <h3 class="box-title" id="curr_bot_descritpion"></h3>
                </div>
            </div>

            <div class="modal-body" style="background-color: #202020">
                <div class="row">
                    <div class="col-md-12">
                        <div class="box box-solid flat no-padding">
                            <div class="box-body">
                                <dl>
                                    <dt>More details</dt>
                                    <dd id="curr_bot_details"></dd>
                                    <br>
                                    <dt>Uses case</dt>
                                    <df id="curr_bot_usecase"></df>
                                </dl>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" id="btnModelDomainInfo" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>