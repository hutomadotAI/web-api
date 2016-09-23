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
                <i class="fa fa-cloud-upload"></i> Upload file
            </button>
            <p></p>
            <input type="file" id="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="No file" data-buttonText="choose file">
            <p></p>
            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUploadFile">
                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                <i class="icon fa fa-check" id="iconAlertUploadFile"></i>
                <span id="msgAlertUploadFile">Before start training you need upload your text file</span>
            </div>
        </div>

        <div class="tab-pane" id="trainingbook">
            <button id="btnUploadStructure" class="btn btn-success btn-sm pull-right flat" style="width: 120px;" disabled>
                <i class="fa fa-cloud-upload"></i> Upload structure
            </button>
            <p></p>
            <input type="file" id="inputstructure" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="No complex file" data-buttonText="choose file">
            <p></p>
            <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUploadStructure">
                <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
                <i class="icon fa fa-check" id="iconAlertUploadStructure"></i>
                <span id="msgAlertUploadStructure">Before start training you need upload your complex text file</span>
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
                <span id="msgAlertUploadUrl">Before start training you need add a url</span>
            </div>
        </div>
       

    </div>



</div>

