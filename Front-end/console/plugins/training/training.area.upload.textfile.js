function uploadTextFile() {
    var Mbyte = 2;

    disableRestartBoxButton(true);

    if (!isTextFileSelected())
        return;
    if ( !checkTextFileSize('inputfile',Mbyte) )
        return;
    
    disableButtonUploadTextFile(true);
    
    var formData = new FormData();
    formData.append("inputfile", document.getElementById('inputfile').files[0]);
    formData.append("tab","file");

    msgAlertUploadFile(1,'Uploading file...');
    $.ajax({
        url : './dynamic/upload.php',
        type : 'POST',
        data : formData,
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function (response) {
            var JSONdata = response;
            var statusCode = JSONdata['status']['code'];

            if (statusCode === 200) {
                var uploadWarnings = null;
                var additionalInfo = JSONdata['status']['additionalInfo'];

                if (additionalInfo != null)
                    uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);

                if (uploadWarnings != null && uploadWarnings.length > 0)
                    msgAlertUploadFile(4, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                else
                    msgAlertUploadFile(4, 'File uploaded');
                setUICurrentStatus(1);
                hideRestartBox();
            } else {
                if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                    msgAlertUploadFile(2, 'File not uploaded. No right content was found.');
                } else {
                    msgAlertUploadFile(2, 'Something has gone wrong. File not uploaded.');
                }
                setUICurrentStatus(-1);
                disableButtonUploadTextFile(false);
                disableRestartBoxButton(false);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertUploadFile(2,'Unexpected error occurred during upload');
            disableButtonUploadTextFile(false);
            disableRestartBoxButton(false);
        }
    });
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

function justStopped(){
    if (document.getElementById('containerMsgWarningAlertTrainingInfo') !== null) 
        return true;
    else 
        return false;
}

function phaseOneReset(){
    document.getElementById('progress-upload-file').style.width = '0%';
    document.getElementById('progress-upload-file-action').className = 'progress progress-xs progress-striped active';
    hidePreTrainingBar(false);
}

function phaseOneUpdate() {
    // simulation phaseOne -  pretraining
    var width = document.getElementById("progress-upload-file").style.width;
    width = width.substr(0, width.length-1);

    if( parseInt(width) <= 100 ){
        document.getElementById("progress-upload-file").style.width = (parseInt(width)+1)+'%';
        document.getElementById('status-badge-upload').innerHTML = width+'%';
        setTimeout(phaseOneUpdate, 100);
    }
    else {
        removeProgressStripedPhaseOne();
        setUICurrentStatus(5);
    }
}

function phaseOneFlashing(flag){
    if (flag) {
        document.getElementById('status-upload-file').innerText = 'initialising';
        document.getElementById('status-upload-file').setAttribute('class', 'text-center flashing');
    }else{
        document.getElementById('status-upload-file').innerText = 'phase 1';
        document.getElementById('status-upload-file').setAttribute('class', 'text-center');
    }
}

function phaseTwoFlashing(flag){
    if (flag) {
        document.getElementById('status-training-file').innerText = 'initialising';
        document.getElementById('status-training-file').setAttribute('class', 'text-center flashing');
    }else{
        document.getElementById('status-training-file').innerText = 'phase 2';
        document.getElementById('status-training-file').setAttribute('class', 'text-center');
    }
}

function phaseOneJump(){
    removeProgressStripedPhaseOne();
    phaseOneMaxValue();
    hidePreTrainingBar(false);
}

function phaseOneMaxValue(){
    document.getElementById('progress-upload-file').style.width = '100%';
    document.getElementById('status-badge-upload').innerHTML = '100%';
}

function removeProgressStripedPhaseOne(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');
}

function hidePreTrainingBar(state){
    $('#pretrainingbar').prop('hidden', state);
}

function hideTrainingBar(state){
    $('#trainingbar').prop('hidden', state);
}

function hideChart(state){
    $('#chart-details').prop('hidden', state);
    $('#chart-details-footer').prop('hidden', state);
}
function phaseTwoActive(){
    disableButtonUploadTextFile(false);
    disableButtonUploadBookFile(false);
    hideTrainingBar(false);
}

function phaseTwoUpdate(error,max_error){
    if ( error > 99)
        error=99; // diplayed on 1% completed time
    var new_value = max_error == 0 ? 0 : (100 - (error *(100 / max_error)));
    // TODO re-define check error limit
    document.getElementById("progress-training-file").setAttribute('value',new_value);
    document.getElementById("progress-training-file").style.width = (parseInt(new_value)) + '%';
    document.getElementById('status-badge-training').innerHTML = parseInt(new_value) + '%';
}

function hideRestartBox(){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        element.parentNode.removeChild(element);
    }
}

function disableRestartBoxButton(state){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        if (state){
            document.getElementById('restart-button').setAttribute('onClick','');
            document.getElementById('restart-button').setAttribute("disabled", "disabled");
        }
        else{
            document.getElementById('restart-button').setAttribute('onClick','trainingRestart()');
            document.getElementById('restart-button').setAttribute("disabled", "enabled");
        }

    }
}