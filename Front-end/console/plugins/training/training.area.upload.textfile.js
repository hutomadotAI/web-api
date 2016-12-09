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
                
                msgAlertProgressBar(1,'Initialising. please wait.');
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

function hideRestartBox(){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        element.parentNode.removeChild(element);
    }
}

function disableRestartBoxButton(state){
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        document.getElementById('restart-button').disabled = state;
    }
}