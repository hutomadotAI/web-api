var TRAINING = TRAINING || (function(){
        var _args = {}; // private
        return {
            init : function(Args) {
                _args = Args;
                showStart(_args[0],_args[1],_args[2]);
            }
        };
    }());


function showStart(status,filename,nameAI){
    var wHTMLFile = "";
    var newNodeFile = document.createElement('div');
    newNodeFile.className = '';
    newNodeFile.id = 'infoTrainingFile';

    var wHTMLBook= "";
    var newNodeBook = document.createElement('div');
    newNodeBook.className = '';
    newNodeBook.id = 'infoTrainingBook';

    var wHTMLWeb= "";
    var newNodeWeb = document.createElement('div');
    newNodeWeb.className = '';
    newNodeWeb.id = 'infoTrainingWeb';


    wHTMLFile += trainingAreaFile(nameAI,0);
    newNodeFile.innerHTML = wHTMLFile;
    document.getElementById('boxTrainingFile').appendChild(newNodeFile);
    writeScript(0);

    wHTMLBook += trainingAreaFile(nameAI,1);
    newNodeBook.innerHTML = wHTMLBook;
    document.getElementById('boxTrainingBook').appendChild(newNodeBook);
    writeScript(1);

    wHTMLWeb += trainingAreaFile(nameAI,2);
    newNodeWeb.innerHTML = wHTMLWeb;
    document.getElementById('boxTrainingWeb').appendChild(newNodeWeb);
    writeScript(2);
}


function trainingAreaFile(nameAI,TAB){
    var wHTML='';
    var msg;
    var placeholder;
    var chooseButtonLabel;
    var uploadButtonLabel;

    switch(TAB){
        case(0): // upload file
            msg ='Before start training you need upload your text file';
            placeholder = 'No file';
            chooseButtonLabel = ' choose file ';
            uploadButtonLabel = 'Upload file';
            break;
        case(1): // upload book
            msg ='Before start training you need upload your complex structure';
            placeholder = 'No complex structure ';
            chooseButtonLabel = ' choose structure ';
            uploadButtonLabel = 'Upload sctucture';
            break;
        case(2): // upload web
            msg ='Before start training you need add an URL';
            placeholder = 'insert URL here';
            chooseButtonLabel = ' add URL ';
            uploadButtonLabel = 'Upload URL';
            break;
        default: msg ='Oops.. Something is wrong!!';
    }


    var wHTMLdata='';
    wHTMLdata += ('data-iconName="glyphicon glyphicon-inbox" ');
    wHTMLdata += ('data-buttonName="btn-success btn-sm flat" ');
    wHTMLdata += ('data-placeholder="'+placeholder+'" ');
    wHTMLdata += ('data-buttonText=" '+chooseButtonLabel+' " ');

    wHTML += ('<p></p>');
    if ( TAB !='2') {
        wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id ="btnUpload' + TAB + '" onClick="uploadFile(' + TAB + ')" style="width: 120px;">');
        wHTML += ('<i class="fa fa-cloud-upload"></i> ' + uploadButtonLabel);
        wHTML += ('</div>');

        wHTML += ('<p></p>');
        wHTML += ('<input type="file" id="inputfile' + TAB + '" name="inputname' + TAB + '" class="filestyle" ' + wHTMLdata + '>');
        wHTML += ('<p></p>');
    }
    if ( TAB =='2') {
        wHTML += ('<div class="input-group">');
        wHTML += ('<input type="text" class="form-control" style="height:30px;" id="inputURL" name="inputURL" placeholder="'+placeholder+'" onkeyup="activeNext(this.value,'+TAB+')">');
        wHTML += ('<span class="input-group-btn">');
        wHTML += ('<div class="btn btn-success btn-sm pull-right flat disabled" id ="btnUpload'+TAB+'" onClick="uploadURL('+ TAB +')" style="width: 120px;">');
        wHTML += ('<i class="fa fa-cloud-upload"></i> '+uploadButtonLabel);
        wHTML += ('</div>');
        wHTML += ('</span>');
        wHTML += ('</div>');
    }
    wHTML += ('<p></p>');
    wHTML += ('<div class="alert alert-base alert-dismissable flat">');
    wHTML += ('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>');
    wHTML += ('<i class="icon fa fa-check"></i>');
    wHTML += ('<span id="msgUpload'+TAB+'">'+msg+'</span>');
    wHTML += ('</div>');
    wHTML += ('<form method="post">');
    wHTML += ('<input type="hidden" id="tab" name="tab" value="'+TAB+'">');
    wHTML += ('</form>');

    return wHTML;
}


function helpInfoSection(){
    var wHTML = "";
    wHTML += ('<div class="btn-group pull-right">');
    wHTML += ('<a data-toggle="collapse"  href="#collapseInfo">');
    wHTML += ('<div class=" pull-right">more info');
    wHTML += ('<i class="fa fa-question-circle text-md text-yellow"></i>');
    wHTML += ('</div>');
    wHTML += ('</a>');
    wHTML += ('</div>');
    return wHTML;
}


function writeScript(TAB){
    var wHTML="";
    var script = document.createElement('script');
    script.id = 'uploadExperience';
    document.getElementsByTagName('head')[0].appendChild(script);

    if ( TAB != 2) {
        wHTML += 'var input = document.getElementById("inputfile' + TAB + '");';
        wHTML += 'input.onclick = function () { this.value = null; };';
        wHTML += 'input.onchange = function () { changeUIState(' + TAB + '); };';
    }
    else{
        wHTML += 'var input = document.getElementById("inputURL");';
        wHTML += 'input.onclick = function () { this.value = null; };';
        wHTML += 'input.onchange = function () { changeUIState(' + TAB + '); };';
    }

    script.text = wHTML;
    document.getElementsByTagName('head')[0].appendChild(script);
}


function changeUIState(TAB){
    var msg;
    switch(TAB){
        case(0): // upload file
            msg ='Now you can upload your file';
            break;
        case(1): // upload book
            msg ='Now you can upload your file your complex structure';
            break;
        case(2): // upload web
            msg ='Now you can upload your URL';
            break;
        default: msg ='Oops.. Something is wrong!!';
    }
    $('#btnUpload'+TAB).attr('disabled', false);
    $('#btnUpload'+TAB).removeClass('btn btn-success btn-sm pull-right flat disabled').addClass('btn btn-success btn-sm pull-right flat');
    try {
        document.getElementById('msgUpload'+TAB).textContent = msg;
    }catch (e) {
    }
}


function uploadFile(TAB){
    var isDisabled =  $('#btnUpload'+TAB).attr('class') == 'btn btn-success btn-sm pull-right flat';
    var msgUploaded ='';
    var msg;

    switch(TAB){
        case(0): // upload file
            msgUploaded ='File uploaded!!';
            break;
        case(1): // upload book
            msgUploaded ='Complex structure uploaded!!';
            break;
        case(2): // upload web
            msgUploaded ='URL uploaded!!';
            break;
        default: msg ='Oops.. Something is wrong!!';
    }
    var msgError = 'Something is gone wrong.';
    if (isDisabled) {

        var xmlhttp;
        var file_data = new FormData();
        file_data.append("inputfile"+TAB, document.getElementById('inputfile'+TAB).files[0]);
        file_data.append("tab",TAB);

        if (window.XMLHttpRequest)
            xmlhttp = new XMLHttpRequest();
        else
            xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

        xmlhttp.open('POST','upload.php');

        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                var JSONresponse = xmlhttp.responseText;
                try {
                    var JSONdata = JSON.parse(JSONresponse);
                    if (JSONdata['status']['code'] === 200)
                        document.getElementById("msgUpload"+TAB).textContent = msgUploaded;
                    else
                        document.getElementById("msgUpload"+TAB).textContent = msgError;
                }catch (e){
                    alert(JSONresponse);
                }
                $('#btnUpload'+TAB).removeClass('btn btn-success btn-sm pull-right flat disabled').addClass('btn btn-success btn-sm pull-right flat');
                $('#trainingBody').css("cursor", "default");
                $('#btnUpload'+TAB).css("cursor", "pointer");
            }
        };
        $('#trainingBody').css("cursor", "progress");
        $('#btnUpload'+TAB).css("cursor", "progress");
        $('#btnUpload'+TAB).removeClass('btn btn-success btn-sm pull-right flat').addClass('btn btn-success btn-sm pull-right flat disabled');
        xmlhttp.send(file_data,TAB);
    }
}

function learnRegExp(url){
    return /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(url);
}

function uploadURL(TAB){
    var isDisabled =  $('#btnUpload'+TAB).attr('class') == 'btn btn-success btn-sm pull-right flat';
    var msgUploaded ='URL uploaded!!';

    var msgError = 'Something is gone wrong.';
    if (isDisabled) {

        var xmlhttp;
        var file_data = new FormData();
        file_data.append("tab",TAB);

        if (window.XMLHttpRequest)
            xmlhttp = new XMLHttpRequest();
        else
            xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

        xmlhttp.open('POST','upload.php');

        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                var JSONresponse = xmlhttp.responseText;
                try {
                    var JSONdata = JSON.parse(JSONresponse);
                    if (JSONdata['status']['code'] === 200)
                        document.getElementById("msgUpload"+TAB).textContent = msgUploaded;
                    else
                        document.getElementById("msgUpload"+TAB).textContent = msgError;
                }catch (e){
                    alert(JSONresponse);
                }
                $('#btnUpload'+TAB).removeClass('btn btn-success btn-sm pull-right flat disabled').addClass('btn btn-success btn-sm pull-right flat');
                $('#trainingBody').css("cursor", "default");
                $('#btnUpload'+TAB).css("cursor", "pointer");
            }
        };
        $('#trainingBody').css("cursor", "progress");
        $('#btnUpload'+TAB).css("cursor", "progress");
        $('#btnUpload'+TAB).removeClass('btn btn-success btn-sm pull-right flat').addClass('btn btn-success btn-sm pull-right flat disabled');
        xmlhttp.send(file_data,TAB);
    }
}


function activeNext(str,TAB) {
    var url = $("#inputURL").val();
    if ( learnRegExp(url) ){
        $("#btnUpload"+TAB).removeClass("btn btn-success btn-sm pull-right flat disabled").addClass("btn btn-success btn-sm pull-right flat");
    }
    else{
        $("#btnUpload"+TAB).removeClass("btn btn-success btn-sm pull-right flat").addClass("btn btn-success btn-sm pull-right flat disabled");
    }
}

function updateStateAI(){
    //$('#btnRefresh').toggleClass('fa-spin');
    $('#btnRefresh').addClass("fa-spin");

}

function buttonGetMoreInfo(){
    var wHTML="";
    wHTML += ('<div id="collapseInfo" class="panel-collapse collapse">');
    wHTML += ('<div class="box-body">');
    wHTML += ('<div class="overlay center-block">');

    wHTML += drawTrainingMoreInfo();

    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>')
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
