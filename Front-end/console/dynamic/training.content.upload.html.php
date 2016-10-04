<div class="nav-tabs-custom no-shadow flat">
    <ul class="nav nav-tabs pull-right">
        <!-- <li><a href="#trainingweb" data-toggle="tab">Video</a></li> -->
        <li><a href="#trainingbook" data-toggle="tab">Book Pages</a></li>
        <li class="active"><a href="#trainingfile" data-toggle="tab">Chat Examples</a></li>
        <li class="pull-left text-md"><i class="fa fa-cloud-upload text-info" style="padding-top:10px;padding-left:10px;padding-right:10px;"></i>AI Training</li>
    </ul>

    <div class="tab-content">

        <div class="tab-pane active" id="trainingfile">
            <button id="btnUploadFile" class="btn btn-success btn-sm pull-right flat" style="width: 120px;" disabled>
                <i class="fa fa-cloud-upload"></i> Upload
            </button>
            <p></p>
            <input type="file" id="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="Select a file..." data-buttonText="choose file">
            <p></p>
            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUploadFile">
                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                <i class="icon fa fa-check" id="iconAlertUploadFile"></i>
                <span id="msgAlertUploadFile">Upload a text file with chat examples to begin training</span>
            </div>

            check out our <a data-toggle="collapse"  href="#collapseVideoTutorialTraining">Training video tutorial</a>
            <p></p>

            <div id="collapseVideoTutorialTraining" class="panel-collapse collapse">
                <div class="box-body flat" >
                    <div class="overlay center-block">
                        <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                            <iframe
                                src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                                frameborder="0" allowfullscreen>
                            </iframe>
                        </div>
                    </div>
                </div>
            </div>

        </div>

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

            check out our <a data-toggle="collapse"  href="#collapseVideoTutorialTrainingBook">Training video tutorial</a>
            <p></p>

            <div id="collapseVideoTutorialTrainingBook" class="panel-collapse collapse">
                <div class="box-body flat" >
                    <div class="overlay center-block">
                        <div class="embed-responsive embed-responsive-16by9" id="videoIntents01">
                            <iframe
                                src="//www.youtube.com/embed/N4IMIpgUVis?controls=1&hd=1&enablejsapi=1"
                                frameborder="0" allowfullscreen>
                            </iframe>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>


        <div class="tab-pane" id="trainingweb">
            <p></p>
            
                <div class="bootstrap-filestyle input-group" id="GrpEntityButton">
                    <input type="text" id="inputurl" class="form-control input-sm " placeholder="add here web address..." style="width: 96%;" >
                    <div class="input-group-btn" tabindex="0">
                        <button id="btnUploadUrl" name="url" class="btn btn-success btn-sm flat pull-right " style="width: 120px;" disabled> <i class="fa fa-globe"></i> Add URL</button>
                    </div>
                </div>
            
            <p></p>

            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUploadUrl">
                <i class="icon fa fa-check" id="iconAlertUploadUrl"></i>
                <span id="msgAlertUploadUrl">Add a URL to begin training</span>
            </div>


        </div>
       

    </div>



</div>


