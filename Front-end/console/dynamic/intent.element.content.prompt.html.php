<!-- Modal PROMPT -->
<div class="modal fade" id="boxPrompts" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal Prompt content-->
        <div class="modal-content bordered" style="background-color: #202020">
            
            <div class="modal-header">
                <button type="button" class="close text-gray" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-tag text-md text-white" style="padding-right:2em"></i> INSERT PROMPTS</h4>
            </div>

            <table class="table table-condensed no-padding no-margin no-border">
                <tr>
                    <th  style="width:33.3333336%; padding-top:15px;" >
                        <div class="text-center">
                            <i class="fa fa-commenting-o text-green"></i>
                            Intent
                        </div>
                    </th>
                    <th  style="width:33.3333336%; padding-top:15px;"  >
                        <div class="text-center">
                            <i class="fa fa fa-sitemap text-md text-md text-yellow"></i>
                            Entity
                        </div>
                    </th>
                    <th  style="width:33.3333336%; padding-top:15px;" >
                        <div class="text-center">
                            <i class="fa fa-sliders text-md text-red"></i>
                            N°prompts
                        </div>
                    </th>
                </tr>
                <tr style="padding-top: 5px;">
                    <td class="text-center" style="width:33.3333336%; padding-left:10px;" >
                        <input type="text-center" class="form-control flat no-shadow no-border text-center" name="curr_intent" value="" style="text-align:center;  background-color: #515151;" disabled/>
                    </td>
                    <td class="text-center" style="width:33.3333336%;">
                        <input type="text" class="form-control flat no-shadow" name="curr_entity" id="curr_entity" value="" style="text-align:center; background-color: #515151;" disabled/>
                    </td>
                    <td class="text-center" style="width:33.3333336%; padding-right:10px;">
                        <input type="text" class="form-control flat no-shadow" name="curr_n_prompts" value=""  style="text-align:center; background-color: #515151;" disabled/>
                    </td>

                </tr>
            </table>
            <br>


            <div class="box-body no-margin" style="padding-top: 0;">
                <div class="row">
                    <div class="col-md-12">
                        <h5 class="box-title">
                            <div class="form-group has-info">
                                <div class="inner-addon left-addon">
                                    <i class="fa fa-tag text-md text-blue"></i>
                                    <input type="text" class="form-control flat no-shadow" id="input-prompt" name="input-prompt" placeholder="add prompt text" onkeydown="checkInputPromptCode(this,event.keyCode)"  style="padding-left: 35px;">
                                </div>
                            </div>
                            <span id="alertMsgPrompt"></span>
                        </h5>
                    </div>
                </div>
            </div>

            <div class="box-body no-margin" id="prompts-list"  style="padding-top: 0;">
                <!-- filled by prompt list -->
            </div>

            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="./dynamic/xxx.php">
                    <button type="button" class="btn btn-primary flat" id="btnModelPromptClose" data-dismiss="modal">Close</button>
                </form>
            </div>


        </div>

    </div>
</div>

