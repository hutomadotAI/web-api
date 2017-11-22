/**
 * Create and send an Upload Training Event to the GTM
 * @return {undefined}
 */
function createUploadTrainingEvent(eventname, name, aiid) {
    if ('dataLayer' in window) {
        dataLayer.push({
            event: 'abstractEvent',
            eventCategory: 'training',
            eventAction: 'upload',
            eventLabel: eventname,
            eventMetadata: {
                timestamp: Date.now(),
                aiid: aiid,
                name: name
            }
        });
    }
}

function uploadTextFile() {
    var maximumFileSize = 512 * 1024; // 512k

    disableRestartBoxButton(true);

    if (!isTextFileSelected())
        return;
    if (!checkTextFileSize('inputfile', maximumFileSize))
        return;

    disableButtonUploadTextFile(true);

    var formData = new FormData();
    formData.append("inputfile", document.getElementById('inputfile').files[0]);
    formData.append("tab", "file");

    msgAlertUploadFile(ALERT.WARNING.value, 'Uploading file...');
    $.ajax({
        url: './proxy/upload.php',
        type: 'POST',
        data: formData,
        processData: false, // tell jQuery not to process the data
        contentType: false, // tell jQuery not to set contentType
        success: function(response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    // We do not know aiid at this point - that's why we use "UNKNOWN"
                    createUploadTrainingEvent(AI + "_" + user.email, user.email, "UNKNOWN");
                    var uploadWarnings = null;
                    var additionalInfo = JSONdata['status']['additionalInfo'];

                    if (additionalInfo != null)
                        uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);

                    if (uploadWarnings != null && uploadWarnings.length > 0)
                        msgAlertUploadFile(ALERT.PRIMARY.value, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                    else
                        msgAlertUploadFile(ALERT.PRIMARY.value, 'File uploaded.');

                    showAlertMessageFromUI(UI_STATE.FILE_UPLOADED.value);
                    setUICurrentStatus(UI_STATE.FILE_UPLOADED.value);
                    startPollForStatus();
                    break;
                case 400:
                    var message = JSONdata['status']['info'];
                    if (haNoContentError(JSONdata['status']['additionalInfo'])) {
                        $("#containerMsgAlertUploadFile").attr('class', 'alert alert-dismissable flat alert-danger');
                        $("#iconAlertUploadFile").attr('class', 'icon fa fa-warning');
                        document.getElementById('msgAlertUploadFile').innerHTML = message === null ?
                            'There was a problem reading your file. Please check that the content follows our structure. ' +
                            'You can load a sample file  <a data-toggle="modal" data-target="#sampleTrainingFile" onMouseOver="this.style.cursor=\'pointer\'">here</a>' :
                            message;
                    } else
                        msgAlertUploadFile(ALERT.DANGER.value, message === null ? 'Something has gone wrong. File not uploaded.' : message);

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
        error: function(xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertUploadFile(ALERT.DANGER.value, 'Unexpected error occurred, please re-upload the training file.');
            disableButtonUploadTextFile(false);
            disableRestartBoxButton(false);
        }
    });
}

function isTextFileSelected() {
    var elementValue = document.getElementById("inputfile").value;
    if (elementValue == null || elementValue == "") {
        disableButtonUploadTextFile(true);
        msgAlertUploadFile(ALERT.WARNING.value, 'You need to choose a file first.');
        return false;
    }
    return true;
}

function checkTextFileSize(fileID, size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadFile(ALERT.DANGER.value, 'The file API isn\'t supported on this browser.');
        return false;
    }
    if (!input.files) {
        msgAlertUploadFile(ALERT.DANGER.value, 'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }

    file = input.files[0];
    if (file.size > size) {
        msgAlertUploadFile(ALERT.DANGER.value, 'Sorry, the file size exceeds our limit.');
        return false;
    }

    return true;
}

function enableUploadTextFile() {
    if ($(this).val() == null || $(this).val == "")
        disableButtonUploadTextFile(true);
    else
        disableButtonUploadTextFile(false);
    msgAlertUploadFile(ALERT.BASIC.value, 'You can now upload your file.');
}

function disableButtonUploadTextFile(state) {
    document.getElementById("btnUploadFile").disabled = state;
}