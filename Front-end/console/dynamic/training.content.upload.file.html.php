<div class="tab-pane active" id="trainingfile">

    <button id="btnUploadFile" class="btn btn-success btn-sm pull-right flat" style="width: 120px;" disabled>
        <i class="fa fa-cloud-upload"></i> Upload
    </button>

    <p></p>

    <input type="file" id="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="Select a file..." data-buttonText="choose file">
    <p></p>

    <div class="alert alert-dismissable flat alert-warning" id="containerMsgAlertUploadFile">
        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
        <i class="icon fa fa-check" id="iconAlertUploadFile"></i>
        <span id="msgAlertUploadFile">Upload a text file with chat examples to begin training</span>
    </div>

    <!-- link video tutorial -->
    check our <a data-toggle="collapse"  href="#collapseVideoTutorialTraining">Training video tutorial</a>

    <!-- link sample of training file MODAL-->
    <div class="pull-right">
        link to
        <a data-toggle="modal" data-target="#sampleTrainingFile" onMouseOver="this.style.cursor='pointer'"> Sample training file</a>
    </div>

    <!-- video tutorial container -->
    <div id="collapseVideoTutorialTraining" class="panel-collapse collapse">
        <div class="box-body flat" >
            <div class="overlay center-block">
                <div class="embed-responsive embed-responsive-16by9" id="videoTrainingFile">
                    <iframe
                        src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                        frameborder="0" allowfullscreen>
                    </iframe>
                </div>
            </div>
        </div>
    </div>

</div>

<!-- MODAL Example training file -->
<div class="modal fade" id="sampleTrainingFile" role="dialog">
    <div class="modal-dialog flat">

        <!-- Modal content-->
        <div class="modal-content bordered">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">   <i class="fa fa-file-text-o text-success" style="padding-right:2em"></i> Training file sample</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="example-training-file">
                    <span id="contentSampleFile"></span>
                    <?php echo file_get_contents('./dist/file/sampleTrainingFile.txt');?>
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary flat" id="btnModalUploadSample" data-dismiss="modal">Upload</button>
                <button type="button" class="btn btn-primary flat" id="btnModalCancelSample" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>

