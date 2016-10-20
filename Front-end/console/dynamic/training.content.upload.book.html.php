<div class="tab-pane" id="trainingbook">

    <button id="btnUploadStructure" class="btn btn-success btn-sm pull-right flat" style="width: 120px;" disabled>
        <i class="fa fa-cloud-upload"></i> Upload Pages
    </button>

    <p></p>

    <input type="file" id="inputstructure" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="Select a file..." data-buttonText="choose file">
    <p></p>

    <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUploadStructure">
        <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
        <i class="icon fa fa-check" id="iconAlertUploadStructure"></i>
        <span id="msgAlertUploadStructure">Upload a page from a book to begin training</span>
    </div>

    <!-- link video tutorial -->
    check our <a data-toggle="collapse"  href="#collapseVideoTutorialTrainingBook">Training video tutorial</a>

    <!-- link sample of training file MODAL-->
    <div class="pull-right">
        link to
        <a data-toggle="modal" data-target="#sampleTrainingBook" onMouseOver="this.style.cursor='pointer'"> Sample book training file</a>
    </div>

    <!-- video tutorial container -->
    <div id="collapseVideoTutorialTrainingBook" class="panel-collapse collapse">
        <div class="box-body flat" >
            <div class="overlay center-block">
                <div class="embed-responsive embed-responsive-16by9" id="videoTrainingBook">
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
<div class="modal fade" id="sampleTrainingBook" role="dialog">
    <div class="modal-dialog flat">

        <!-- Modal content-->
        <div class="modal-content bordered">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">   <i class="fa fa-file-text-o text-success" style="padding-right:2em"></i> Training book file sample</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="example-training-book">
                    <span id="contentSampleFile"></span>
                    <?php echo file_get_contents('./dist/file/sampleTrainingBook.txt');?>
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary flat" id="btnModalUploadBookSample" data-dismiss="modal">Upload</button>
                <button type="button" class="btn btn-primary flat" id="btnModalCancelBookSample" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>


