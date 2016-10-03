var max_error = -1.0;
var block_server_ping = false;

initializedEventListeners();
activeMonitors(status,error);

function initializedEventListeners(){
    document.getElementById('inputfile').addEventListener('change', enableUploadFile);
    document.getElementById('inputstructure').addEventListener('change', enableUploadStructure);
    document.getElementById('inputurl').addEventListener('keyup', enableUploadUrl);

    document.getElementById('btnUploadFile').addEventListener('click', uploadFile);
    document.getElementById('btnUploadStructure').addEventListener('click', uploadStructure);
    document.getElementById('btnUploadUrl').addEventListener('click', uploadUrl);
}

function activeMonitors(status,error){

    if (status != 0)
        activePhaseOne();
    if ( status == 'training_in_progress' || status == 'training_queued') {
        activePhaseTwo();
        pingError();
    }
    if ( status == 'internal_error') {
        activePhaseTwo();
    }

}

function enableUploadFile() {
    if ( $(this).val() == null || $(this).val == "")
        $('#btnUploadFile').prop('disabled', true);
    else
        $('#btnUploadFile').prop('disabled', false);
    msgAlertUploadFile(0,'You can now upload your file');
}

function enableUploadStructure() {
    // real implemented
    if ( $(this).val() == null || $(this).val == "")
        $("#btnUploadStructure").prop("disabled", true);
    else
        $("#btnUploadStructure").prop("disabled", false);
    msgAlertUploadStructure(0,'You can now upload your complex file');


    // for demo
    //$("#btnUploadStructure").prop("disabled", true);
}

function enableUploadUrl() {
    /* real implemented
    var url = $('#inputurl').val();
    if ( learnRegExp(url) )
        $('#btnUploadUrl').prop('disabled', false);
    else
        $("#btnUploadUrl").prop("disabled", true);
    */

    // for demo
    $("#btnUploadUrl").prop("disabled", true);
}


function resetPhaseTwoComponents(){
    document.getElementById("progress-training-file").style.width ='0%';
    document.getElementById('status-bagde-training').innerHTML = '0%';
    document.getElementById('containerMsgAlertProgressBar').style.display = 'none';
}

function getUploadWarnings(info) {
    var warnings = [];
    var maxItemsToShow = 5;
    var itemsToShow = Math.min(info.length, maxItemsToShow);
    for (var i = 0; i < itemsToShow; i++) {
        if (info[i]['key'] === 'UPLOAD_MISSING_RESPONSE') {
            warnings.push("Missing response for '" + info[i]['value'] + "'");
        }
    }
    if (info.length > maxItemsToShow) {
        warnings.push("...");
    }
    return warnings;
}

function haNoContentError(info) {
    for (var i = 0; i < info.length; i++) {
        if (info[i]['key'] === 'UPLOAD_NO_CONTENT') {
            return true;
        }
    }
    return false;
}

function uploadFile(){
    if ( $('#inputfile').val() == null ||  $('#inputfile').val() == "") {
        $("#btnUploadFile").prop("disabled", true);
        msgAlertUploadFile(1,'You need to choose a file first');
        return;
    };

    if ( !checkFileSize('inputfile',2) )
        return;

    resetPhaseTwoComponents();
    block_server_ping = true;
    $("#btnUploadFile").prop("disabled", true);


    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputfile", document.getElementById('inputfile').files[0]);
    file_data.append("tab","file");

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
                var statusCode = JSONdata['status']['code'];
                if (statusCode === 200) {
                    var uploadWarnings = null;
                    var additionalInfo = JSONdata['status']['additionalInfo'];
                    if (additionalInfo != null) {
                        uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);
                    }
                    if (uploadWarnings != null && uploadWarnings.length > 0) {
                        msgAlertUploadFile(4, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    } else {
                        msgAlertUploadFile(4, 'File uploaded');
                    }
                    resetPhaseOneComponents();
                    updatePhaseOneComponents();
                } else {
                    if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                        msgAlertUploadFile(2, 'File not uploaded. No content was found.');
                    } else {
                        msgAlertUploadFile(2, 'Something has gone wrong. File not uploaded');
                    }
                    $("#btnUploadFile").prop("disabled", false);
                }
            } catch (e) {
                msgAlertUploadFile(2,'A generic error occurred');
                $("#btnUploadFile").prop("disabled", false);
            }
        }
    };
    msgAlertUploadFile(1,'Uploading file...');
    xmlhttp.send(file_data);
}


function uploadStructure(){
    if ( $('#inputstructure').val() == null ||  $('#inputstructure').val() == "") {
        $("#btnUploadStructure").prop("disabled", true);
        msgAlertUploadStructure(1,'You need to choose a complex file first');
        return;
    }

    if ( !checkFileSize('inputstructure',10) )
        return;

    resetPhaseTwoComponents();
    block_server_ping = true;
    $("#btnUploadStructure").prop("disabled", true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputstructure", document.getElementById('inputstructure').files[0]);
    file_data.append("tab","structure");

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
                if (JSONdata['status']['code'] === 200) {
                    msgAlertUploadStructure(4, 'Complex file Uploaded!!!');
                    resetPhaseOneComponents();
                    updatePhaseOneComponents();
                }
                else{
                    msgAlertUploadStructure(2,'Something has gone wrong. Complex file not uploaded');
                    $("#btnUploadStructure").prop("disabled", false);
                }
            }catch (e){
                msgAlertUploadStructure(2,'Something has gone wrong; JSON error on complex file transfer');
                $("#btnUploadStructure").prop("disabled", false);
            }
        }
    };
    msgAlertUploadStructure(1,'Uploading complex file...');
    xmlhttp.send(file_data);
}


function uploadUrl(){
    if ( $('#inputurl').val() == null ||  $('#inputurl').val() == "") {
        $("#btnUploadUrl").prop("disabled", true);
        msgAlertUploadStructure(1,'You need to choose a complex file first');
        return;
    }
    else
        $("#btnUploadUrl").prop("disabled", true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("tab","url");

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
                    msgAlertUploadUrl(4,'Video Uploaded!!!');
                else
                    msgAlertUploadUrl(2,'Something has gone wrong. Video not uploaded');
            }catch (e){
                msgAlertUploadUrl(2,'Something has gone wrong. JSON error on video transfer');
            }
            $("#btnUploadUrl").prop("disabled", false);
        }
    };
    msgAlertUploadUrl(1,'Uploading Video...');
    xmlhttp.send(file_data);
}

function resetPhaseOneComponents(){
    document.getElementById('progress-upload-file').style.width = '0%';
    document.getElementById('progress-upload-file-action').className = 'progress progress-xs progress-striped active';
}

function updatePhaseOneComponents() {
    // simulation of loading phaseOne
    var width = document.getElementById("progress-upload-file").style.width;
    width = width.substr(0, width.length-1);

    if( parseInt(width) <= 100 ){
        document.getElementById("progress-upload-file").style.width = (parseInt(width)+1)+'%';
        //var rgb =  getGreenToRed(parseInt(width));
        //document.getElementById('status-bagde-upload').style.backgroundColor = rgb;
        document.getElementById('status-bagde-upload').innerHTML = width+'%';
        setTimeout(updatePhaseOneComponents, 100);
    }
    else {
        activePhaseTwo();
        //pingError();
    }
}

function activePhaseOne(){
    msgAlertUploadFile(0, 'A file is already loaded');
    document.getElementById('progress-upload-file').style.width = '100%';
    document.getElementById('status-bagde-upload').innerHTML = '100%';
}

function activePhaseTwo(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');


    $('#btnUploadFile').prop('disabled', false);
    $('#trainingbar').prop('hidden', false);

    // force flashing initialization text
    document.getElementById('status-training-file').innerText ='initialising';
    document.getElementById('status-training-file').setAttribute('class', 'text-center flashing');

    document.getElementById('containerMsgAlertProgressBar').style.display = 'block';

    msgAlertProgressBar(0,'You can now talk to your AI.');
    //closingMsgAlertProgressBarTemporized();
    block_server_ping = false;
}

function notifyError(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');
    msgAlertProgressBar(2,'Internal error occured');
}

function pingError(){
    var width = document.getElementById("progress-training-file").style.width;
    width = width.substr(0, width.length-1);
    if( (parseInt(width) <= 100) && !block_server_ping) {
        pingErrorCall();
        setTimeout(pingError, 5000);
    }
}


function pingErrorCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','./dynamic/pingError.php');

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {

            // global page error variable
            error = xmlhttp.responseText;
            try {
                if( error != 'error' && !block_server_ping) {

                    if( parseInt(error)!= 0) {
                        document.getElementById('status-training-file').innerText ='phase 2';
                        document.getElementById('status-training-file').setAttribute('class', 'text-center');
                    }

                    if ( parseFloat(error) > parseFloat(max_error))
                        max_error = error;

                    var new_width = 100 - (error *(100 /max_error));
                    document.getElementById("progress-training-file").style.width = (parseInt(new_width)) + '%';
                    document.getElementById('status-bagde-training').innerHTML = parseInt(new_width) + '%';
                }
                else {
                    msgAlertUploadStructure(2,'Something has gone wrong. Update status training ERROR');
                }
            }catch (e){
                msgAlertUploadStructure(2,'Something has gone wrong. Update status training ERROR');
            }
        }
    };
    xmlhttp.send();
}

function getGreenToRed(percent){
    var rgb = new Array();
    r = percent<50 ? 255 : Math.floor(255-(percent*2-100)*255/100);
    g = percent>50 ? 255 : Math.floor((percent*2)*255/100);
    return 'rgb('+r+','+g+',0)';
}


function learnRegExp(url){
    return /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(url);
}

function checkFileSize(fileID,size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadFile(2,'The file API isn\'t supported on this browser');
        return false;
    }
    if (!input.files) {
        msgAlertUploadFile(2,'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }
    file = input.files[0];
    if(file.size > size*1048476) {
        msgAlertUploadFile(2, 'The file size exceeds the limit allowed and cannot be uploaded.');
        return false;
    }
    return true;
}

function checkStructureSize(fileID,size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadStructure(2,'The file API isn\'t supported on this browser');
        return false;
    }
    if (!input.files) {
        msgAlertUploadStructure(2,'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }
    file = input.files[0];
    if(file.size > size*1048476) {
        msgAlertUploadStructure(2, 'The file size exceeds the limit allowed and cannot be uploaded.');
        return false;
    }
    return true;
}



// VIDEO TUTORIAL TRAINING CHAT EXAMPLE
$("#collapseVideoTutorialTraining").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});

// VIDEO TUTORIAL TRAINING BOOK
$("#collapseVideoTutorialTrainingBook").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});