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




                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">   <i class="fa fa-file-text-o text-success" style="padding-right:2em"></i> Training file sample</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="example-training-file">
                    <span id="contentSampleFile"></span>
                    <dt>hello my phone is broken</dt>
                    <dt>oh sorry what is the problem</dt>
                    <dt>the screen does not turn on</dt>
                    <dt>hmmm ok. have you tried charging</dt>
                    <dt>it is charged i think</dt>
                    <dt>ok what happens when you press the power button</dt>
                    <dt>nothing happens</dt>
                    <dt>ok let me send you someone to pick up your phone</dt>
                    </br>
                    <dt>hello</dt>
                    <dt>hi, how are you</dt>
                    <dt>i am fine thanks! and you?</dt>
                    <dt>I am good</dt>
                    </br>
                    <dt>what is your name</dt>
                    <dt>my name is AI1</dt>
                    </br>
                    <dt>what does your name mean?</dt>
                    <dt>not sure. ask maurizio</dt>
                    </br>
                    <dt>waht is the meaning of life</dt>
                    <dt>the meaning of life is 42</dt>
                    <?php //echo file_get_contents('./dist/file/sampleTrainingFile.txt');?>
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary flat" id="btnModalUploadSample" data-dismiss="modal">Upload</button>
                <button type="button" class="btn btn-primary flat" id="btnModalCancelSample" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>



