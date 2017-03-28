<div class="box box-solid box-clean flat no-shadow unselectable" id="box_monitor">

    <div class="box-header with-border">
        <i class="fa fa-cloud-upload text-info"></i>
        <div class="box-title"><b>Upload a text File</b></div>
    </div>
    <div class="box-body">

        <button id="btnUploadFile" class="btn btn-success btn-sm pull-right flat" style="width: 120px;" disabled>
            <i class="fa fa-cloud-upload"></i> upload
        </button>

        <input type="file" id="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox"
               data-buttonName="btn-success btn-sm flat" data-placeholder="select txt file"
               data-buttonText="select txt file">
        <p></p>
        <div class="alert alert-dismissable flat alert-base no-margin" id="containerMsgAlertUploadFile" style="margin-bottom: 0px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertUploadFile"></i>
            <span id="msgAlertUploadFile">Upload a plain text file with chat examples to begin training. Look at <a
                    data-toggle="modal" data-target="#sampleTrainingFile" onMouseOver="this.style.cursor='pointer'">this example</a>.
            </span>
        </div>
    </div>
    <div class="box-footer">
            <!-- link video tutorial -->
            check out our <a data-toggle="collapse"  href="#collapseVideoTutorialTraining">training video tutorial</a>

            <!-- link sample of training file MODAL-->
            <div class="pull-right">
                link to
                <a data-toggle="modal" data-target="#sampleTrainingFile" onMouseOver="this.style.cursor='pointer'"> sample training file</a>
            </div>

            <!-- video tutorial container -->
            <div id="collapseVideoTutorialTraining" class="panel-collapse collapse">
                <div class="box-body flat" >
                    <div class="overlay center-block">
                        <div class="embed-responsive embed-responsive-16by9" id="videoTrainingFile">
                            <iframe
                                src="//www.youtube.com/embed/__pO6wVvBEY?controls=1&hd=1&enablejsapi=1"
                                frameborder="0" allowfullscreen>
                            </iframe>
                        </div>
                    </div>
                </div>
            </div>
    </div>
</div>

<!-- MODAL Example training file -->
<div class="modal fade" id="sampleTrainingFile" role="dialog">
    <div class="modal-dialog flat">

        <!-- Modal content-->
        <div class="modal-content bordered" style="background-color: #212121">
            <div class="modal-header bordered">
                <button type="button" class="close text-gray" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">   <i class="fa fa-file-text-o text-success" style="padding-right:2em"></i><span class="unselectable">Training file sample</span> </h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="example-training-file">
                    <span id="contentSampleFile"></span>
                    <dt>What is your name?</dt>
                    <dt>My name is Hu:toma.</dt>
                    </br>
                    <dt>What does Hu:toma mean?</dt>
                    <dt>It is the combination of two words, human and automata.</dt>
                    </br>
                    <dt>What is the meaning of life?</dt>
                    <dt>If you find the answer, let me know.</dt>
                    </br>
                    <dt>Where do you live?</dt>
                    <dt>Barcelona & London.</dt>
                    </br>
                    <dt>How old are you?</dt>
                    <dt>I am 8760 hours old.</dt>
                    </br>
                    <dt>Can we be friends?</dt>
                    <dt>Yes of course we can be friends!</dt>
                    </br>
                    <dt>Good morning Hu:toma</dt>
                    <dt>Good morning to you too my friend.</dt>
                    </br>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" id="btnModalCancelSample" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>

<input type="hidden" id="training-status" name="training-status" value="0" style="display:none;"/>
<input type="hidden" id="training-error" name="training-error" value="-1" style="display:none;"/>
<input type="hidden" id="training-progress-phase1" name="training-progress-phase1" value="-1" style="display:none;"/>
<input type="hidden" id="training-progress-phase2" name="training-progress-phase2" value="-1" style="display:none;"/>