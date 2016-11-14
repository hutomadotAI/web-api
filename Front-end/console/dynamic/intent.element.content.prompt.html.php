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


            <div class="box-body no-margin" id="boxIntentPrompt" style="padding-top: 0;">

                <div class="row">
                    <div class="col-md-12">
                        <h5 class="box-title">
                            <div class="input-group no-margin">
                                <input type="text" class="form-control flat no-shadow" id="intent-prompt" name="input-prompt"
                                       placeholder="Add prompt text" onkeyup="checkInputPromptCode(this,event.keyCode)"
                                       style="width: 96%;">
                                <span class="input-group-btn">
                                    <button class="btn btn-success flat" id="btnAddIntentPrompt" style="width: 130px;" disabled>Add Prompt</button>
                                </span>
                            </div>
                        </h5>
                    </div>
                </div>

                <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertIntentPrompt" style="margin-bottom:10px;">
                    <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                    <i class="icon fa fa-check" id="iconAlertIntentPrompt"></i>
                    <span id="msgAlertIntentPrompt">Give the AI examples of prompt.</span>
                </div>

            </div>

            <div class="box-body no-margin" id="prompts-list" style="padding-top: 0;">
                <!-- filled by prompt list -->
            </div>

            <div class="modal-footer">
                    <button type="button" class="btn btn-primary flat" id="btnModelPromptClose" data-dismiss="modal">Close</button>
            </div>


        </div>

    </div>
</div>

