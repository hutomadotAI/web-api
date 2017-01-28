var scale_chart_max_error = 10000;
var ai_status_last = "";

initializeEventListeners();
initializeConsole(aiStatus);


function pollStatus() {
    trainingStatusCall();
    getUIStatusCall();
}

function startPollForStatus() {
    setInterval(pollStatus, 2000);
}

function stopPollForStatus() {
    clearInterval(pollStatus);
}

function initializeEventListeners() {
    document.getElementById('inputfile').addEventListener('change', enableUploadTextFile);
    document.getElementById('btnUploadFile').addEventListener('click', uploadTextFile);
}

function initializeConsole(aiStatus) {
    setStateResponse(aiStatus);

    if (aiStatus["training_file_uploaded"] != 0) {
        msgAlertUploadFile(ALERT.PRIMARY.value, 'A file is already loaded.');
        setStateResponse(aiStatus);
    }

    switch(aiStatus['ai_status']){
        case 'ai_undefined':
            stopPollForStatus();
            break;
        case 'ai_training_complete' :
            stopPollForStatus();
            hidePreTrainingBar(true);
            msgAlertProgressBar(ALERT.SUCCESS.value, 'Training completed.');
            break;
        default:
            startPollForStatus();
    }
}


function getUIStatusCall() {
    var state = parseInt(getUICurrentStatus());
    switch (true) {
        case (state == 0): //nothing is started
            break;
        case (state == 1): // File uploaded
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. please wait.');
            phaseOneFlashing(true);
            trainingStartCall();
            break;
        case (state == 2): //  Initialising - Phase One
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. please wait.');
            break;
        case (state == 3): //Initialising - Phase One - queue
            phaseOneFlashing(false);
            phaseQueue();
            hideTrainingBar(true);
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. please wait. The process is queued');
            break;
        case (state == 4): // Execute - Phase One ( simulation ) waiting API
            if (document.getElementById('status-badge-upload').innerHTML == '0%') {
                phaseOneFlashing(false);
                phaseOneReset();
                phaseOneUpdate();
                document.getElementById('show-error').innerText = 'not yet available';
                msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase one in progress...');
            }
            break;
        case (state == 5): // Initialising - Phase Two
            phaseOneFlashing(false);
            phaseOneJump();
            phaseTwoFlashing(true);
            hideChart(true);
            phaseTwoActive();
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialization Phase two may take one minute. please wait.');
            break;
        case (state == 6): // start phase two
            var progress = getP2Progress();
            phaseOneJump();
            hideChart(false);
            phaseTwoActive();
            phaseTwoFlashing(false);
            phaseTwoUpdate(progress);
            document.getElementById('show-error').innerText = deep_error;
            msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase two in progress...');
            break;
        case (state == 7):
            msgAlertProgressBar(ALERT.WARNING.value, 'Training stopped. Please restart training');
            if (!justStopped()) {
                createMessageWarningInfoAlert();
                hideChart(true);
                hidePreTrainingBar(true);
                hideTrainingBar(true);
            }
            break;
        case (state == 10):
            stopPollForStatus();
            phaseTwoFlashing(false);
            phaseTwoMaxValue();
            msgAlertProgressBar(ALERT.SUCCESS.value, 'Training completed.');
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            break;
        case (state == -1):
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            break;
        default:
    }
}

function trainingStartCall() {
    jQuery.ajax({
        url: './dynamic/trainingStart.php',
        type: 'GET',
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            var JSONdata = response;
            var statusCode = JSONdata['status']['code'];
            //TODO temporary coded - code 400 returned when you upload file on existing file
            if ((statusCode === 200 ) || (statusCode === 400 )) {
                setUICurrentStatus(2);
            } else {
                msgAlertProgressBar(ALERT.DANGER.value, 'Training cannot start! code error ' + statusCode);
                setUICurrentStatus(-1);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(-1);
            msgAlertProgressBar(ALERT.DANGER.value, 'Unexpected error occurred during start training');
        }
    });
}

function trainingStatusCall() {
    jQuery.ajax({
        url: './dynamic/trainingStatusAI.php',
        type: 'GET',
        //dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            try {
                var jsonData = JSON.parse(response);
                if (jsonData['api_status']['code'] === 200) {
                    setStateResponse(jsonData);
                } else {
                    setUICurrentStatus(-1);
                    msgAlertProgressBar(ALERT.DANGER.value, 'An error has occurred while trying to get the AI\'s status');
                }
            } catch (e) {
                setUICurrentStatus(-1);
                msgAlertProgressBar(ALERT.DANGER.value, 'Unable to query AI training status');
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(-1);
            msgAlertProgressBar(ALERT.DANGER.value, 'Cannot contact server to query AI status');
        }
    });
}

function setStateResponse(aiStatus) {

    deep_error = aiStatus["deep_learning_error"];
    var status = aiStatus["ai_status"];
    var phaseTwoPercentProgress = aiStatus["phase_2_progress"] * 100.0;
    setP2Progress(phaseTwoPercentProgress);

    switch (status) {
        case 'ai_error' :
            //setP2Progress(phaseTwoPercentProgress);
            setUICurrentStatus(-1);
            msgAlertProgressBar(ALERT.DANGER.value, 'An error has occurred.');
            break;
        case 'ai_ready_to_train':
            setP2Progress(0);
            if (status != ai_status_last) {
                setUICurrentStatus(1);
            }
            // code 0
            break;
        case 'ai_training_queued' :
            setP2Progress(0);
            closeMessageWarningInfoAlert();
            setUICurrentStatus(3);  // code 3
            break;
        case 'ai_training':
            closeMessageWarningInfoAlert();
            switch (true) {
                case ( deep_error > 0 ):
                    setUICurrentStatus(6);
                    break;
                case ( deep_error == 0 ):
                    setUICurrentStatus(5);
                    break;
            }
            break;
        case 'ai_training_stopped' :
            setUICurrentStatus(7); // code 6
            break;
        case 'ai_training_complete' :
            setP2Progress(100);
            closeMessageWarningInfoAlert();
            setUICurrentStatus(10); // code 10
            break;
        case 'ai_undefined' :
        default:
            setUICurrentStatus(999);
            break;
    }
    ai_status_last = status;
}

function trainingRestart() {
    disableButtonUploadTextFile(true);

    phaseOneReset();
    hideTrainingBar(true);
    hideChart(true);

    disableRestartBoxButton();
    setUICurrentStatus(1);

    disableButtonUploadTextFile(false);
}

function getUICurrentStatus() {
    var result = document.getElementById('training-status').value;
    setStateListeningMode();
    return result;
}

function setUICurrentStatus(status) {
    document.getElementById('training-status').value = status;
}

function setStateListeningMode() {
    setUICurrentStatus(999);
}

function getP2Progress() {
    return document.getElementById('training-progress').value;
}

function setP2Progress(progress) {
    document.getElementById('training-progress').value = progress.toFixed(7);
}

function getUICurrentMaxError() {
    return document.getElementById('training-max-error').value;
}

function setUICurrentMaxError(error) {
    document.getElementById('training-max-error').value = error;
}

function learnRegExp(url) {
    return /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(url);
}

function createMessageWarningInfoAlert() {
    var wHTML = ''

    wHTML += ('<div class="alert alert-dismissable flat alert-warning" id="containerMsgWarningAlertTrainingInfo">');
    wHTML += ('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
    wHTML += ('<span id="msgAlertWarningTrainingInfo">');
    wHTML += ('<dt>Start info pre-trained neural Manipulation</dt>');
    wHTML += ('<dd>');
    wHTML += ('The goals of the training program should relate directly to the needs determined by the assessment process outlined above.');
    wHTML += ('Course objectives should clearly state what behavior or skill will be changed as a result of the training and should relate');
    wHTML += ('</dd>');
    wHTML += ('<p></p>');
    wHTML += ('<dt class="text-center">');
    wHTML += ('<button class="btn btn-primary btn-md center-block flat" id="restart-button" onclick="trainingRestart();"> <b>Restart Training</b></button>');
    wHTML += ('</dt>');

    wHTML += ('</span>');
    wHTML += ('</div>');

    var parent = document.getElementById('trainingBox');
    parent.innerHTML = wHTML;
}


function closeMessageWarningInfoAlert() {
    if (justStopped()) {
        var node = document.getElementById('containerMsgWarningAlertTrainingInfo');
        var parent = node.parentNode;
        parent.innerHTML = '';
    }
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

// VIDEO TUTORIAL TRAINING CHAT EXAMPLE
$("#collapseVideoTutorialTraining").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});