var TRAINING = TRAINING || (function(){
    var _args = {}; // private
    return {
        init : function(Args) {
            _args = Args;

            _args[0]=0;
            _args[1]=0;
            showStart(_args[0],_args[1]);
            showUpload(_args[0],_args[1]);
            writeScript()
        },
    };
}());


function showStart(status,file){

    var newNode = document.createElement('div');
    newNode.className = '';
    newNode.id = 'infoTraining';
    
    var wHTML = "";

    if ( file == null || file =="undefined" || file == '0' )
        wHTML += drawTrainingInfo();
    else
        wHTML += drawTrainingStart(status);

    newNode.innerHTML = wHTML;
    document.getElementById('boxTraining').appendChild(newNode);
}

function drawTrainingInfo(){
    var wHTML="";
    wHTML += ('<div class="box-header no-border">');

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-exclamation-circle text-md text-danger" id="iconTraining" style="margin-top: 8px; margin-right: 8px;"></i>');
    wHTML += ('You need upload the training file.');
    wHTML += ('</div>');

    wHTML += ('<a data-toggle="collapse"  href="#collapseInfo">');
    wHTML += ('<div class="btn btn-success btn-sm pull-right flat" style="margin-right: 5px; width: 120px;">');
    wHTML += ('<i class="fa fa-download"></i> Get more info');
    wHTML += ('</div>');
    wHTML += ('</a>');

    wHTML += ('</div>');

    wHTML += ('<div id="collapseInfo" class="panel-collapse collapse">');
    wHTML += ('<div class="box-body">');
    wHTML += ('<div class="overlay center-block">');

    wHTML += drawTrainingMoreInfo();

    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');

    return wHTML;
}


function drawTrainingStart(status){

    var wHTML='';
    wHTML += ('<div class="box-header no-border">');

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-check-circle-o text-md text-success" id="iconTraining" style="margin-top:8px; margin-right: 8px;"></i>');
    wHTML += ('Ready for start Training.');
    wHTML += ('</div>');
    wHTML += drawRightButtons(status);

    wHTML += ('</div>');

    return wHTML;
}


function drawTrainingMoreInfo() {
    var wHTML = "";
    wHTML += ('<section class="content bg-gray-light" >');

    wHTML += ('<div class="box-body">');
    wHTML += ('<dl class="dl-horizontal">');
    wHTML += ('<dt>Description Actions</dt>');
    wHTML += ('<dd>Before start training process, y.</dd>');
    wHTML += ('<dt>Euismod</dt>');
    wHTML += ('<dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>');
    wHTML += ('<dd>Donec id elit non mi porta gravida at eget metus.</dd>');
    wHTML += ('<dt>Malesuada porta</dt>');
    wHTML += ('<dd>Etiam porta sem malesuada magna mollis euismod.</dd>');
    wHTML += ('<dt>Felis euismod semper eget lacinia</dt>');
    wHTML += ('<dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>');
    wHTML += ('</dl>');
    wHTML += ('</div>');
    wHTML += ('</section>');
    wHTML += ('<p></p>');
    wHTML +="need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>.";

    return wHTML;
}


function drawRightButtons(status){
    var wHTML = "";
    switch (status) {
        case 0 ://Queued
            wHTML += ('<div class="btn btn-success btn-sm pull-right flat" id="btnTraining" style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> start training');
            wHTML += ('</div>');
            break;
        case 1 ://Training
            wHTML += ('<div class="btn btn-warning btn-sm pull-right flat" id="btnTraining" style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> stop training');
            wHTML += ('</div>');
            break;
        case 2 ://Trained
            wHTML += ('<div class="btn btn-success btn-sm pull-right flat" id="btnTraining" style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> start training');
            wHTML += ('</div>');
            break;
        case 3 ://Stopping
            wHTML += ('<div class="btn btn-warning btn-sm pull-right flat" id="btnTraining" style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> stop training');
            wHTML += ('</div>');
            break;
        case 4 ://Stopped
            wHTML += ('<div class="btn btn-primary btn-sm pull-right flat" id="btnTraining" style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> resume training');
            wHTML += ('</div>');
            break;
        case 5 ://Limited
            wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id="btnTraining" data-toggle="tooltip" title="you have reach the limit of AIs training"style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-graduation-cap"></i> limit reached');
            wHTML += ('</div>');
            break;
        default://Error
            wHTML += ('<div class="btn btn-danger btn-sm pull-right disabled" id="btnTraining" data-toggle="tooltip" title="Unaspected AI status"style="margin-right: 5px; width: 120px;">');
            wHTML += ('<i class="fa fa-warning"></i> training blocked');
            wHTML += ('</div>');
    }
    return wHTML;
}


function showUpload(status,file){

    var newNode = document.createElement('div');
    newNode.className = '';
    newNode.id = 'infoTraining';

    var wHTML = "";


    wHTML += drawUploadInfo(file);

    newNode.innerHTML = wHTML;
    document.getElementById('boxUpload').appendChild(newNode);
}


function drawUploadInfo(file){
    var filename =  'example.txt';
    var wHTML="";
    wHTML += ('<div class="box-header no-border">');

    if (file==0) {

        wHTML += ('<div class="btn-group pull-left">');
        wHTML += ('<i class="fa fa-exclamation-circle text-md text-danger" id="iconUpload" style="margin-top: 8px;margin-right: 8px;"></i>');
        wHTML += ('Any file uploaded');
        wHTML += ('</div>');

        wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id ="btnUpload" style="margin-right: 5px; width: 120px;">');
        wHTML += ('<i class="fa fa-cloud-upload"></i> Upload file');
        wHTML += ('</div>');


    }
    else {
        wHTML += ('<div class="btn-group pull-left">');
        wHTML += ('<i class="fa fa-check-circle-o text-md text-success" id="iconUpload" style="margin-top: 8px; margin-right: 8px;"></i>');
        wHTML += ('Last uploaded file is <label>'+ filename+'</label>');
        wHTML += ('</div>');


        wHTML += ('<div class="btn btn-success btn-sm pull-right flat" id ="btnUpload" style="margin-right: 5px; width: 120px;">');
        wHTML += ('<i class="fa fa-cloud-upload"></i> Upload file');
        wHTML += ('</div>');
    }

    wHTML += ('</div>');

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-exclamation-circle text-md text-danger" id="iconFile" style="margin-top: 18px;margin-right: 8px; margin-left: 10px;"></i>');
    wHTML += ('</div>');
    wHTML += ('<div class="box-footer">');

    wHTML += ('<input type="file" id="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="No file" data-buttonText=" choose file ">');
    wHTML += ('</div>');

    return wHTML;
}


function writeScript(){
    var wHTML="";
    var script = document.createElement('script');
    script.id = 'uploadExperience';
    document.getElementsByTagName('head')[0].appendChild(script);


    wHTML +='var input = document.getElementById("inputfile");';
    wHTML +='input.onclick = function () { this.value = null; };';
    wHTML +='input.onchange = function () { alert(this.value); };';

    script.text = wHTML;

    document.getElementsByTagName('head')[0].appendChild(script);
}


