var TRAINING = TRAINING || (function(){
    var _args = {}; // private
    return {
        init : function(Args) {
            _args = Args;
            showStart(_args[0],_args[1]);
            writeScript()
        },
    };
}());


function showStart(status,filename){
    var wHTML = "";
    var newNode = document.createElement('div');
    newNode.className = '';
    newNode.id = 'infoTraining';

    if ( status == 0)
        wHTML += trainingAreaUnchecked();
    else
        wHTML += trainingAreaChecked(filename);

    newNode.innerHTML = wHTML;
    document.getElementById('boxTraining').appendChild(newNode);
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


function writeScript(){
    var wHTML="";
    var script = document.createElement('script');
    script.id = 'uploadExperience';
    document.getElementsByTagName('head')[0].appendChild(script);

    wHTML +='var input = document.getElementById("inputfile");';
    wHTML +='input.onclick = function () { this.value = null; };';
    wHTML +='input.onchange = function () { changeUIState(); };';

    script.text = wHTML;
    document.getElementsByTagName('head')[0].appendChild(script);
}


function trainingAreaUnchecked(){
    var wHTML='';
    var msg ='You need upload the training file first.';

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-exclamation-circle text-md text-danger" id="iconFile" style="margin-top: 18px;margin-right: 8px; margin-left: 10px;"></i>');
    wHTML += ('</div>');

    wHTML += ('<div class="box-header">');
    wHTML += ('<input type="file" id="inputfile" name="inputname" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="No file" data-buttonText=" choose file ">');
    wHTML += ('</div>');

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-exclamation-circle text-md text-danger" id="iconUpload" style="margin-top: 18px; margin-right: 8px;margin-left: 10px;"></i>');
    wHTML += ('<div id="msgUpload" style="display:inline;">');
    wHTML += (msg);
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="box-header">');
    wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id ="btnUpload"  onClick="uploadFile()" style="margin-right: 5px; width: 120px;">');
    wHTML += ('<i class="fa fa-cloud-upload"></i> Upload file');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += buttonGetMoreInfo();
    return wHTML;
}


function trainingAreaChecked(filename){
    var wHTML='';
    var msg ='The last training file is ';

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class=" fa fa-check-circle-o text-md text-success" id="iconFile" style="margin-top: 18px;margin-right: 8px; margin-left: 10px;"></i>');
    wHTML += ('</div>');

    wHTML += ('<div class="box-header">');
    wHTML += ('<input type="file" id="inputfile" name="inputfile" class="filestyle" data-iconName="glyphicon glyphicon-inbox" data-buttonName="btn-success btn-sm flat" data-placeholder="No file" data-buttonText=" choose file ">');
    wHTML += ('</div>');

    wHTML += ('<div class="btn-group pull-left">');
    wHTML += ('<i class="fa fa-check-circle-o text-md text-success" id="iconUpload" style="margin-top: 18px; margin-right: 8px;margin-left: 10px;"></i>');
    wHTML += ('<div id="msgUpload" style="display:inline;">');
    wHTML += (msg+'<label>'+filename+'</label>');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="box-header">');
    wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id ="btnUpload" onClick="uploadFile()"; style="margin-right: 5px; width: 120px;">');
    wHTML += ('<i class="fa fa-cloud-upload"></i> Upload file');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += buttonGetMoreInfo();

    return wHTML;
}


function changeUIState(){
    $("#btnUpload").attr("disabled", false);
    $('#btnUpload').removeClass('btn btn-success btn-sm pull-right flat disabled').addClass('btn btn-success btn-sm pull-right flat');
    $('#iconUpload').removeClass('fa fa-exclamation-circle text-md text-danger').addClass('fa fa-check-circle-o text-md text-success');
    $('#iconFile').removeClass('fa fa-exclamation-circle text-md text-danger').addClass('fa fa-check-circle-o text-md text-success');

    document.getElementById("msgUpload").textContent='Now you can upload your file';
}


function uploadFile(){
    var isDisabled =  $('#btnUpload').attr('class') == 'btn btn-success btn-sm pull-right flat';
    if (isDisabled) {

        var xmlhttp;
        var file_data = new FormData();
        file_data.append("inputfile", document.getElementById('inputfile').files[0]);

        if (window.XMLHttpRequest)
            xmlhttp = new XMLHttpRequest();
        else
            xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

        xmlhttp.open('POST','upload.php');

        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                var JSONresponse = xmlhttp.responseText;

                var JSONdata = JSON.parse(JSONresponse);
                if(JSONdata['status']['code'] === 200)
                    document.getElementById("msgUpload").textContent = 'File uploaded!!';
                else
                    document.getElementById("msgUpload").textContent = 'Something is gone wrong. File NOT uploaded.';
                $('#btnUpload').removeClass('btn btn-success btn-sm pull-right flat disabled').addClass('btn btn-success btn-sm pull-right flat');
                $('#trainingBody').css("cursor", "default");
                $('#btnUpload').css("cursor", "pointer");
            }
        };
        $('#trainingBody').css("cursor", "progress");
        $('#btnUpload').css("cursor", "progress");
        $('#btnUpload').removeClass('btn btn-success btn-sm pull-right flat').addClass('btn btn-success btn-sm pull-right flat disabled');
        xmlhttp.send(file_data);
    }
}


function buttonGetMoreInfo(){
    var wHTML="";
    wHTML += ('<div class="box-header">');
    wHTML += ('<a data-toggle="collapse"  href="#collapseInfo">');
    wHTML += ('<div class="btn btn-primary btn-sm pull-right flat" style="margin-right: 5px; width: 120px;">');
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
    wHTML += ('</div>')
    return wHTML;
}