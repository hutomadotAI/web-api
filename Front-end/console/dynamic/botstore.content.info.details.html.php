<!--
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 20/10/16
 * Time: 14:16
-->
<!-- Modal PROMPT -->
<div class="modal fade" id="boxBotStoreInfo" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal Prompt content-->
        <div class="modal-content padding" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-tag text-md text-white" style="padding-right:2em"></i>
                    <span name="curr_bot_name" value=""></span>
                </h4>
            </div>

            <div class="box-body no-margin" style="padding-top: 0;">
                <div class="row">
                    <div class="col-md-12">
                        <div class="box box-solid">
                            <div class="box-header with-border">
                                <h3 class="box-title" id="curr_bot_descritpion"></h3>
                            </div>
                            <div class="box-body">
                                <dl>
                                    <dt>More details</dt>
                                    <dd id="curr_bot_details"></dd>
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

