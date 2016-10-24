function uploadTextFile(){
    var Mbyte = 2;

    if (!isTextFileSelected())
        return;

    if ( !checkTextFileSize('inputfile',Mbyte) )
        return;

    block_server_ping = true;
    resetPhaseTwoComponents();
    disableButtonUploadTextFile(true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputfile", document.getElementById('inputfile').files[0]);
    file_data.append("tab","file");

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
                        msgAlertUploadFile(4, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    } else {
                        msgAlertUploadFile(4, 'File uploaded');
                    }
                    resetTrainingTextFilePhaseOneComponents();
                    updateTrainingTextFilePhaseOneComponents();

                    // if exits box-warning re-start - it needs disabled
                    hideMsgWarningAlertTrainingInfo();
                } else {
                    if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                        msgAlertUploadFile(2, 'File not uploaded. No content was found.');
                    } else {
                        msgAlertUploadFile(2, 'Something has gone wrong. File not uploaded');
                    }
                    disableButtonUploadTextFile(false);
                }
            } catch (e) {
                msgAlertUploadFile(2,'A generic error occurred');
                disableButtonUploadTextFile(false);
            }
        }
    };

    msgAlertUploadFile(1,'Uploading file...');
    xmlhttp.send(file_data);
}

function isTextFileSelected(){
    var elementValue = document.getElementById("inputfile").value;
    if ( elementValue == null ||  elementValue == "") {
        disableButtonUploadTextFile(true);
        msgAlertUploadFile(1,'You need to choose a file first');
        return false;
    }
    return true;
}

function checkTextFileSize(fileID,size) {
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

function enableUploadTextFile() {
    if ( $(this).val() == null || $(this).val == "")
        disableButtonUploadTextFile(true);
    else
        disableButtonUploadTextFile(false);
    msgAlertUploadFile(0,'You can now upload your file');
}

function disableButtonUploadTextFile(state){
    document.getElementById("btnUploadFile").disabled = state;
}

function resetTrainingTextFilePhaseOneComponents(){
    document.getElementById('progress-upload-file').style.width = '0%';
    document.getElementById('progress-upload-file-action').className = 'progress progress-xs progress-striped active';
}

function updateTrainingTextFilePhaseOneComponents() {
    // simulation of loading phaseOne
    var width = document.getElementById("progress-upload-file").style.width;
    width = width.substr(0, width.length-1);

    msgAlertProgressBar(0,'Phase 1 processing... ');

    if( parseInt(width) <= 100 ){
        document.getElementById("progress-upload-file").style.width = (parseInt(width)+1)+'%';
        document.getElementById('status-badge-upload').innerHTML = width+'%';
        setTimeout(updateTrainingTextFilePhaseOneComponents, 10);
    }
    else {
        removeProgressStripedPhaseOne();
        trainingStartCall();
    }
}

function activeTrainingTextFilePhaseTwo(){
    disableButtonUploadTextFile(false);
    hideTrainingBar(false);
    initialisingStatusTrainingBar(true);
    block_server_ping = false;
}


function initialisingStatusTrainingBar(state){
    if (state) {
        document.getElementById('status-training-file').innerText = 'initialising';
        document.getElementById('status-training-file').setAttribute('class', 'text-center flashing');
        msgAlertProgressBar(0,'Training initialization may take a few minutes. please wait.');

    }else{
        document.getElementById('status-training-file').innerText = 'phase 2';
        document.getElementById('status-training-file').setAttribute('class', 'text-center');
        msgAlertProgressBar(0,'Now you can talk with your AI');
    }
}

function hideTrainingBar(state){
    $('#trainingbar').prop('hidden', state);
}


function updateTrainingBar(error,max_error){
    var new_width = max_error == 0 ? 0 : (100 - (error *(100 / max_error)));
    // TODO re-define check error limit
    if (new_width > 100 )
        new_width = 100;
    
    document.getElementById("progress-training-file").style.width = (parseInt(new_width)) + '%';
    document.getElementById('status-badge-training').innerHTML = parseInt(new_width) + '%';
}


function jumpPhaseOne(){
    msgAlertUploadFile(0, 'A file is already loaded');
    removeProgressStripedPhaseOne();
    setProgressPhaseOneMaxValue();
}

function jumpPhaseTwo(){
    msgAlertProgressBar(0,'Training finished');
    removeProgressStripedPhaseTwo();
    setProgressPhaseTwoMaxValue();
    hideTrainingBar(false);
}

function setProgressPhaseOneMaxValue(){
    document.getElementById('progress-upload-file').style.width = '100%';
    document.getElementById('status-badge-upload').innerHTML = '100%';
}

function removeProgressStripedPhaseOne(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');
}

function setProgressPhaseTwoMaxValue(){
    document.getElementById('progress-training-file').style.width = '100%';
    document.getElementById('status-badge-training').innerHTML = '100%';
}

function removeProgressStripedPhaseTwo(){
    $('#progress-training-file-action').removeClass('active');
    $('#progress-training-file-action').removeClass('progress-striped');
}


function resetPhaseTwoComponents(){
    //document.getElementById('container_startstop').style.display = 'none';
    document.getElementById("progress-training-file").style.width ='0%';
    document.getElementById('status-badge-training').innerHTML = '0%';
    hideTrainingBar(true);
}


function hideMsgWarningAlertTrainingInfo(){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        //remove Warning BOX in re-strat training
        //element.parentNode.removeChild(element);

        // disable only restart button
        document.getElementById('restart-button').disabled = true;
    }
}





/* START and  STOP  functions

function showStartStopButton(){
    document.getElementById('container_startstop').style.display = 'block';
}


function startStop() {
    if (document.getElementById('startstop-button').getAttribute('value') == '_start') {
        msgAlertProgressBar(0,'Training process launched.');
        switchToStop();
    }
    else {
        msgAlertProgressBar(0,'Training process stopped.');
        switchToStart();
    }
}

function switchToStart(){
    var elem_icon = document.getElementById('startstop-icon');
    var elem_btn = document.getElementById('startstop-button');
    var elem_text = document.getElementById('text-startstop');

    elem_btn.setAttribute('value','_start');
    elem_btn.setAttribute('class', 'btn btn-app text-light-blue');
    elem_icon.setAttribute('class', 'fa fa-play no-margin text-light-blue');
    elem_text.innerText ='resume training';
    console.log('call stop training');

}

function switchToStop(){
    var elem_icon = document.getElementById('startstop-icon');
    var elem_btn = document.getElementById('startstop-button');
    var elem_text = document.getElementById('text-startstop');

    elem_btn.setAttribute('value','_stop');
    elem_btn.setAttribute('class', 'btn btn-app text-red');
    elem_icon.setAttribute('class', 'fa fa-stop no-margin text-red');
    elem_text.innerText ='stop training';
    console.log('call start/resume training');
}

 */