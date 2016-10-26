function uploadBookFile(){
    var Mbyte = 10;

    //if exists hide it
    disableMsgWarningAlertTrainingInfoRestartButton(true);

    if (!isBookFileSelected())
        return;

    if ( !checkBookFileSize('inputstructure',Mbyte) )
        return;

    block_server_ping = true;

    resetTrainingFilePhaseTwoComponents();
    disableButtonUploadBookFile(true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputfile", document.getElementById('inputfile').files[0]);
    file_data.append("tab","structure");

    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','./dynamic/upload.php');

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
                        msgAlertUploadStructure(4, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    } else {
                        msgAlertUploadStructure(4, 'File uploaded');
                    }

                    resetTrainingBookFilePhaseOneComponents();
                    updateTrainingBookFilePhaseOneComponents();
                    // if exits box-warning re-start - it needs disabled
                    hideMsgWarningAlertTrainingBookInfo();
                } else {
                    if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                        msgAlertUploadStructure(2, 'File not uploaded. No content was found.');
                    } else {
                        msgAlertUploadStructure(2, 'Something has gone wrong. File not uploaded');
                    }
                    disableButtonUploadBookFile(false);
                    disableMsgWarningAlertTrainingBookInfoRestartButton(false);
                }
            } catch (e) {
                msgAlertUploadFile(2,'A generic error occurred');
                disableButtonUploadBookFile(false);
                disableMsgWarningAlertTrainingBookInfoRestartButton(false);
            }
        }
    };

    msgAlertUploadStructure(1,'Uploading file...');
    xmlhttp.send(file_data);
}

function isBookFileSelected(){
    var elementValue = document.getElementById("inputstructure").value;
    if ( elementValue == null ||  elementValue == "") {
        disableButtonUploadBookFile(true);
        msgAlertUploadStructure(1,'You need to choose a file first');
        return false;
    }
    return true;
}

function checkBookFileSize(fileID,size) {
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

function enableUploadBookFile() {
    if ( $(this).val() == null || $(this).val == "")
        disableButtonUploadBookFile(true);
    else
        disableButtonUploadBookFile(false);
    msgAlertUploadStructure(0,'You can now upload your book');
}

function disableButtonUploadBookFile(state){
    document.getElementById("btnUploadStructure").disabled = state;
}

function resetTrainingBookFilePhaseOneComponents(){
    document.getElementById('progress-upload-file').style.width = '0%';
    document.getElementById('progress-upload-file-action').className = 'progress progress-xs progress-striped active';
    hidePreTrainingBar(false);
    hideTrainingBar(true);
}

function updateTrainingBookFilePhaseOneComponents() {
    // simulation phaseOne -  pretraining
    var width = document.getElementById("progress-upload-file").style.width;
    width = width.substr(0, width.length-1);

    msgAlertUploadStructure(0,'Phase 1 processing... ');

    if( parseInt(width) <= 100 ){
        document.getElementById("progress-upload-file").style.width = (parseInt(width)+1)+'%';
        document.getElementById('status-badge-upload').innerHTML = width+'%';
        setTimeout(updateTrainingBookFilePhaseOneComponents, 80);
    }
    else {

        // start training session
        removeProgressStripedPhaseOne();
        trainingStartCall();
    }
}

function activeTrainingBookFilePhaseTwo(){
    disableButtonUploadBookFile(false);
    hideTrainingBar(false);
    initialisingStatusTrainingBar(true);
    block_server_ping = false;
}


function initialisingStatusTrainingBar(state){
    if (state) {
        document.getElementById('status-training-file').innerText = 'initialising';
        document.getElementById('status-training-file').setAttribute('class', 'text-center flashing');
        msgAlertProgressBar(1,'Training initialization may take a few minutes. please wait.');

    }else{
        document.getElementById('status-training-file').innerText = 'phase 2';
        document.getElementById('status-training-file').setAttribute('class', 'text-center');
        msgAlertProgressBar(0,'Now you can talk with your AI');
    }
}

function hideTrainingBar(state){
    $('#trainingbar').prop('hidden', state);
}

function hidePreTrainingBar(state){
    $('#pretrainingbar').prop('hidden', state);
}


function updateTrainingBar(error,max_error){
    var new_value = max_error == 0 ? 0 : (100 - (error *(100 / max_error)));
    // TODO re-define check error limit
    document.getElementById("progress-training-file").setAttribute('value',new_value);
    document.getElementById("progress-training-file").style.width = (parseInt(new_value)) + '%';
    document.getElementById('status-badge-training').innerHTML = parseInt(new_value) + '%';
}


function jumpPhaseOneBook(){
    msgAlertUploadFile(1, 'A file is already loaded');
    removeProgressStripedPhaseOneBook();
    setProgressPhaseOneMaxValueBook();
    hidePreTrainingBar(false);
}

function jumpPhaseTwoBook(){
    msgAlertProgressBar(4,'Training finished');
    removeProgressStripedPhaseTwoBook();
    setProgressPhaseTwoMaxValueBook();
    hideTrainingBar(false);
}

function setProgressPhaseOneMaxValueBook(){
    document.getElementById('progress-upload-file').style.width = '100%';
    document.getElementById('status-badge-upload').innerHTML = '100%';
}

function removeProgressStripedPhaseOneBook(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');
}

function setProgressPhaseTwoMaxValueBook(){
    document.getElementById('progress-training-file').style.width = '100%';
    document.getElementById('status-badge-training').innerHTML = '100%';
}

function removeProgressStripedPhaseTwoBook(){
    $('#progress-training-file-action').removeClass('active');
    $('#progress-training-file-action').removeClass('progress-striped');
}


function resetTrainingBookFilePhaseTwoComponents(){
    //document.getElementById('container_startstop').style.display = 'none';
    hideTrainingBar(true);
    document.getElementById("progress-training-file").style.width ='0%';
    document.getElementById('status-badge-training').innerHTML = '0%';

}


function hideMsgWarningAlertTrainingBookInfo(){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        element.parentNode.removeChild(element);
    }
}

function disableMsgWarningAlertTrainingBookInfoRestartButton(state){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        document.getElementById('restart-button').disabled = state;
    }
}
