function uploadTextFile() {
    var Mbyte = 2;

    disableRestartBoxButton(true);

    if (!isTextFileSelected())
        return;
    if (!checkTextFileSize('inputfile', Mbyte))
        return;

    disableButtonUploadTextFile(true);

    var formData = new FormData();
    formData.append("inputfile", document.getElementById('inputfile').files[0]);
    formData.append("tab", "file");

    msgAlertUploadFile(ALERT.WARNING.value, 'Uploading file...');
    $.ajax({
        url: './dynamic/upload.php',
        type: 'POST',
        data: formData,
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function (response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    var uploadWarnings = null;
                    var additionalInfo = JSONdata['status']['additionalInfo'];
                    
                    if (additionalInfo != null)
                        uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);

                    if (uploadWarnings != null && uploadWarnings.length > 0)
                        msgAlertUploadFile(ALERT.PRIMARY.value, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    else
                        msgAlertUploadFile(ALERT.PRIMARY.value, 'File uploaded');

                    showAlertMessageFromUI(UI_STATE.FILE_UPLOADED.value);
                    setUICurrentStatus(UI_STATE.FILE_UPLOADED.value);
                    startPollForStatus();
                    break;
                case 400:
                    if (haNoContentError(JSONdata['status']['additionalInfo']))
                        msgAlertUploadFile(ALERT.DANGER.value, 'File not uploaded. No right content was found.');
                    else
                        msgAlertUploadFile(ALERT.DANGER.value, 'Something has gone wrong. File not uploaded.');

                    setUICurrentStatus(UI_STATE.ERROR.value);
                    disableButtonUploadTextFile(false);
                    disableRestartBoxButton(false);
                    break;
                case 500:
                    msgAlertUploadFile(ALERT.DANGER.value, JSONdata['status']['info']);
                    setUICurrentStatus(UI_STATE.ERROR.value);
                    disableButtonUploadTextFile(false);
                    disableRestartBoxButton(false);
                    break;
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertUploadFile(ALERT.DANGER.value, 'Unexpected error occurred during upload');
            disableButtonUploadTextFile(false);
            disableRestartBoxButton(false);
        }
    });
}

function isTextFileSelected() {
    var elementValue = document.getElementById("inputfile").value;
    if (elementValue == null || elementValue == "") {
        disableButtonUploadTextFile(true);
        msgAlertUploadFile(ALERT.WARNING.value, 'You need to choose a file first');
        return false;
    }
    return true;
}

function checkTextFileSize(fileID, size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadFile(ALERT.DANGER.value, 'The file API isn\'t supported on this browser');
        return false;
    }
    if (!input.files) {
        msgAlertUploadFile(ALERT.DANGER.value, 'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }

    file = input.files[0];
    if (file.size > size * 1048476) {
        msgAlertUploadFile(ALERT.DANGER.value, 'The file size exceeds the limit allowed and cannot be uploaded.');
        return false;
    }

    return true;
}

function enableUploadTextFile() {
    if ($(this).val() == null || $(this).val == "")
        disableButtonUploadTextFile(true);
    else
        disableButtonUploadTextFile(false);
    msgAlertUploadFile(ALERT.BASIC.value, 'You can now upload your file');
}

function disableButtonUploadTextFile(state) {
    document.getElementById("btnUploadFile").disabled = state;
}