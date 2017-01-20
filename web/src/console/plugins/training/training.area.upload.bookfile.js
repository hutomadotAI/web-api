var timeIsOver = false;

function uploadBookFile(){
    var Mbyte = 10;

    //if exists hide it
    disableMsgWarningAlertTrainingInfoRestartButton(true);

    if (!isBookFileSelected())
        return;

    if ( !checkBookFileSize('inputstructure',Mbyte) )
        return;

    block_server_ping = true;

    resetComponentsPhaseTwo();
    disableButtonUploadBookFile(true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputstructure", document.getElementById('inputstructure').files[0]);
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
                        msgAlertUploadStructure(ALERT.PRIMARY.value, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    } else {
                        msgAlertUploadStructure(ALERT.PRIMARY.value, 'File uploaded');
                    }

                    resetComponentsPhaseOne();
                    launch_safe_timeout_controll();
                    removeProgressStripedPhaseOne();
                    updateComponentsPhaseOneComplexBook();
                    // if exits box-warning re-start - it needs disabled
                    hideMsgWarningAlertTrainingInfo();
                } else {
                    if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                        msgAlertUploadStructure(ALERT.DANGER.value, 'File not uploaded. No content was found.');
                    } else {
                        msgAlertUploadStructure(ALERT.DANGER.value, 'Something has gone wrong. File not uploaded');
                    }
                    disableButtonUploadBookFile(false);
                    disableMsgWarningAlertTrainingInfoRestartButton(false);
                }
            } catch (e) {
                msgAlertUploadStructure(ALERT.DANGER.value,'A generic error occurred');
                disableButtonUploadBookFile(false);
                disableMsgWarningAlertTrainingInfoRestartButton(false);
            }
        }
    };

    msgAlertUploadStructure(ALERT.WARNING.value,'Uploading file...');
    xmlhttp.send(file_data);
}

function isBookFileSelected(){
    var elementValue = document.getElementById("inputstructure").value;
    if ( elementValue == null ||  elementValue == "") {
        disableButtonUploadBookFile(true);
        msgAlertUploadStructure(ALERT.WARNING.value,'You need to choose a file first');
        return false;
    }
    return true;
}

function checkBookFileSize(fileID,size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadStructure(ALERT.DANGER.value,'The file API isn\'t supported on this browser');
        return false;
    }
    if (!input.files) {
        msgAlertUploadStructure(ALERT.DANGER.value,'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }
    file = input.files[0];
    if(file.size > size*1048476) {
        msgAlertUploadStructure(ALERT.DANGER.value, 'The file size exceeds the limit allowed and cannot be uploaded.');
        return false;
    }
    return true;
}

function enableUploadBookFile() {
    if ( $(this).val() == null || $(this).val == "")
        disableButtonUploadBookFile(true);
    else
        disableButtonUploadBookFile(false);
    msgAlertUploadStructure(ALERT.BASIC.value,'You can now upload your structured text');
}

function disableButtonUploadBookFile(state){
    document.getElementById("btnUploadStructure").disabled = state;
}

function updateComponentsPhaseOneComplexBook() {
    // coded only for DEMO
    var width = document.getElementById("progress-upload-file").style.width;
    width = width.substr(0, width.length-1);

    msgAlertProgressBar(0,'Phase 1 pre processing file... ');

    if( parseFloat(width) <= 90 ){

        var status = getAiStatusCall();

        if ( status == 'malformed_training_file') {
            msgAlertUploadStructure(ALERT.DANGER.value, 'The training file is malformed');
            document.getElementById("progress-upload-file").style.width = 0 + '%';
            document.getElementById('status-badge-upload').innerHTML = 0 + '%';
            msgAlertProgressBar(0,'Training not started. Please upload training data.');
            return;
        }

        if ( status != 'preprocessed') {
            document.getElementById("progress-upload-file").style.width = ( (parseFloat(width) + 0.5).toFixed(1)).toString() + '%';
            document.getElementById('status-badge-upload').innerHTML =    ( (parseFloat(width) + 0.5).toFixed(1)).toString() + '%';
            setTimeout(updateComponentsPhaseOneComplexBook, 100);
        }
        else {
            // finish
            document.getElementById("progress-upload-file").style.width = 100 + '%';
            document.getElementById('status-badge-upload').innerHTML = 100 + '%';
            removeProgressStripedPhaseOne();
            trainingStartCall();
        }
    }
    else {
        //wait more time
        waitBook();
    }
}


function waitBook(){
    var status = getAiStatusCall();
    if ( status == 'malformed_training_file') {
        msgAlertUploadStructure(ALERT.DANGER.value, 'The training file is malformed');
        document.getElementById("progress-upload-file").style.width = 0 + '%';
        document.getElementById('status-badge-upload').innerHTML = 0 + '%';
        msgAlertProgressBar(0,'Training not started. Please upload training data.');
        return;
    }

    if ( status != 'preprocessed' && !timeIsOver) {
        alert('poto tempo');
        setTimeout(waitBook, 2000);
    }else {
        // finish
        document.getElementById("progress-upload-file").style.width = 100 + '%';
        document.getElementById('status-badge-upload').innerHTML = 100 + '%';
        removeProgressStripedPhaseOne();
        trainingStartCall();
    }
}

function launch_safe_timeout_controll(){
    // coded only temporary
    setTimeout(expiredTime, 10000); //  4 minutes
}

function expiredTime(){
    timeIsOver = true;
}
