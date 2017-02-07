var UI_STATE =
{
    ERROR: {value: -1},
    NOTHING: {value: 0},
    FILE_UPLOADED: {value: 1},
    READY_TO_TRAIN: {value: 2},
    PHASE1_INIT: {value: 3},
    PHASE1_QUEUE: {value: 4},
    PHASE1_RUN: {value: 5},
    PHASE2_INIT: {value: 6},
    PHASE2_RUN: {value: 7},
    STOPPED: {value: 8},
    COMPLETED: {value: 10},
    LISTENING_MODE:{value:999}
};

var UI_TRAINING_STATE =
{
    PHASE1_INIT: {value: 100},
    PHASE1_RUN: {value: 101},
    PHASE1_END: {value: 102},
    PHASE2_INIT: {value: 200},
    PHASE2_RUN: {value: 201}
};

var ai_status_last = "";

initializeEventListeners();

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

function initializePretrainingMonitor(aiStatus){
    var wHTML ='';
    var parent = document.getElementById('pretrainingbar');
    var phaseOnePercentProgress = getErrorPercentProgress(aiStatus["phase_1_progress"]);

    wHTML +='<td class="text-center" id="status-upload-file">phase 1</td>';
    wHTML +='<td>';
    wHTML +='<div class="progress progress-xs" id="progress-upload-file-action" style="margin-top:9px;">';
    wHTML +='<div class="progress-bar progress-bar-primary" id="progress-upload-file" value="' + phaseOnePercentProgress + '" style="width:' + phaseOnePercentProgress + '%"></div>';
    wHTML +='</div>';
    wHTML +='</td>';
    wHTML +='<td class="text-center"><span id="status-badge-upload" class="badge btn-primary">' + phaseOnePercentProgress + '%</span></td>';

    parent.innerHTML = wHTML;
}

function initializeTrainingMonitor(aiStatus){
    var wHTML ='';
    var parent = document.getElementById('trainingbar');
    var phaseTwoPercentProgress = getErrorPercentProgress(aiStatus["phase_2_progress"]);

    wHTML +='<td class="text-center" id="status-training-file">phase 2</td>';
    wHTML +='<td>';
    wHTML +='<div class="progress progress-xs" id="progress-training-file-action" style="margin-top:9px;">';
    wHTML +='<div class="progress-bar progress-bar-success" id="progress-training-file" value="' + phaseTwoPercentProgress + '" style="width:' + phaseTwoPercentProgress + '%"></div>';
    wHTML +='</div>';
    wHTML +='</td>';
    wHTML +='<td class="text-center" style="width: 120px;"><span id="status-badge-training" class="badge btn-success">' + phaseTwoPercentProgress + '%</span></td>';

    parent.innerHTML = wHTML;

    document.getElementById('show-error').innerText = aiStatus["deep_learning_error"];
}

function initializeAlertMessage(aiStatus) {
    var wHTML ='';
    var parent = document.getElementById('msgAlertBox');

    wHTML +='<div id="containerMsgAlertProgressBar" style="margin-bottom: 0; padding-right:0;">';
    wHTML +='<i id="iconAlertProgressBar"></i>';
    wHTML +='<span id="msgAlertProgressBar"></span>';
    wHTML +='</div>';

    parent.innerHTML = wHTML;

    if (aiStatus["training_file_uploaded"] != 0)
        msgAlertUploadFile(ALERT.PRIMARY.value, 'A file is already loaded.');
}

function initializeTrainingConsole(aiStatus){
    switch(aiStatus['ai_status']){
        case API_AI_STATE.UNDEFINED.value:
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessage(aiStatus['ai_status']);
            break;
        case API_AI_STATE.QUEUED.value:
            phaseOneQueue();
            phaseOneReset();
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessage(aiStatus['ai_status']);
            startPollForStatus();
            break;
        case API_AI_STATE.READY_TO_TRAIN.value:
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessage(aiStatus['ai_status']);
            startPollForStatus();
            break;
        case API_AI_STATE.TRAINING.value:
            var phaseOnePercentProgress = getErrorPercentProgress(aiStatus['phase_1_progress']);
            var phaseTwoPercentProgress = getErrorPercentProgress(aiStatus['phase_2_progress']);
            var deepLearningError = aiStatus['deep_learning_error'];

            switch(true){
                case (phaseOnePercentProgress < 0.0001 ):
                    hideTrainingBar(true);
                    hideChart(true);
                    phaseOneFlashing(true);
                    showAlertMessage(UI_TRAINING_STATE.PHASE1_INIT.value);
                    break;
                case (phaseOnePercentProgress < 99.999):
                    hideTrainingBar(true);
                    hideChart(true);
                    phaseOneStriped(true);
                    showAlertMessage(UI_TRAINING_STATE.PHASE1_RUN.value);
                    break;
                case (phaseOnePercentProgress > 99.999):
                    if (phaseTwoPercentProgress < 0.0001) {

                        phaseOneFlashing(false);
                        phaseOneStriped(false);
                        phaseOneMaxValue();

                        phaseTwoFlashing(true);
                        hideChart(true);

                        showAlertMessage(UI_TRAINING_STATE.PHASE2_INIT.value);
                    }
                    else {
                        phaseOneFlashing(false);
                        phaseOneStriped(false);
                        phaseOneMaxValue();

                        phaseTwoFlashing(false);
                        phaseTwoStriped(true);
                        hideChart(false);

                        phaseTwoUpdate(phaseTwoPercentProgress);
                        updateDeepLearningError(deepLearningError);
                        showAlertMessage(UI_TRAINING_STATE.PHASE2_RUN.value);
                    }
            }

            disableButtonUploadTextFile(false);
            startPollForStatus();
            break;
        case API_AI_STATE.STOPPED.value:
            phaseTwoFlashing(false);
            phaseTwoStriped(false);
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            createMessageWarningInfoAlert();
            showAlertMessage(aiStatus['ai_status']);
            break;
        case API_AI_STATE.COMPLETED.value :
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessage(aiStatus['ai_status']);
            break;
        case API_AI_STATE.ERROR.value:
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessage(aiStatus['ai_status']);
            break;
        default:
    }
}

function getUIStatusCall() {
    var status = parseInt(getUICurrentStatus());
    switch (true) {
        case (status == UI_STATE.NOTHING.value):
            break;
        case (status == UI_STATE.FILE_UPLOADED.value):
            hideRestartBox();
            break;
        case (status == UI_STATE.READY_TO_TRAIN.value):
            hideRestartBox();

            phaseOneReset();
            phaseOneFlashing(true);
            hidePreTrainingBar(false);
            hideTrainingBar(true);

            showAlertMessageFromUI(UI_STATE.PHASE1_INIT.value); // force to have a msg alert with 'initialising'
            trainingStartCall();
            break;
        case (status == UI_STATE.PHASE1_INIT.value):
            phaseOneFlashing(true);
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.PHASE1_QUEUE.value):
            phaseOneFlashing(false);
            phaseOneQueue();
            phaseOneReset();
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.PHASE1_RUN.value):
            var progress = getP1Progress();
            phaseOneFlashing(false);
            phaseOneStriped(true);
            phaseOneUpdate(progress);
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.PHASE2_INIT.value):
            phaseOneFlashing(false);
            phaseOneStriped(false);
            phaseOneMaxValue();

            hideTrainingBar(false);
            phaseTwoFlashing(true);
            hideChart(true);
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.PHASE2_RUN.value):
            var progress = getP2Progress();
            var error = getDeepLearningError();

            phaseTwoFlashing(false);
            phaseOneStriped(true);
            hideChart(false);
            phaseTwoUpdate(progress);
            updateDeepLearningError(error);
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.STOPPED.value):
            if (!justStopped()) {
                createMessageWarningInfoAlert();
                hideChart(true);
                hidePreTrainingBar(true);
                hideTrainingBar(true);
            }
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.COMPLETED.value):
            stopPollForStatus();

            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            showAlertMessageFromUI(status);
            break;
        case (status == UI_STATE.ERROR.value):
            hidePreTrainingBar(true);
            hideTrainingBar(true);
            hideChart(true);
            break;
        default:
    }
}

function setStateResponse(aiStatus) {
    var status = aiStatus["ai_status"];
    switch (status) {
        case  API_AI_STATE.UNDEFINED.value:
            setUICurrentStatus(UI_STATE.LISTENING_MODE.value);
            break;
        case API_AI_STATE.QUEUED.value:
            setUICurrentStatus(UI_STATE.PHASE1_QUEUE.value);
            break;
        case API_AI_STATE.READY_TO_TRAIN.value:
            if (status != ai_status_last)
                setUICurrentStatus(UI_STATE.READY_TO_TRAIN.value);
            break;
        case API_AI_STATE.TRAINING.value:
            closeMessageWarningInfoAlert();
            var phaseOnePercentProgress = getErrorPercentProgress(aiStatus['phase_1_progress']);
            var phaseTwoPercentProgress = getErrorPercentProgress(aiStatus['phase_2_progress']);
            var deepLearningError = aiStatus['deep_learning_error'];

            switch(true){
                case (phaseOnePercentProgress < 0.0001):
                    setUICurrentStatus(UI_STATE.PHASE1_INIT.value);
                    break;
                case (phaseOnePercentProgress < 99.999):
                    setUICurrentStatus(UI_STATE.PHASE1_RUN.value);
                    break;
                case (phaseOnePercentProgress > 99.999):
                    if (phaseTwoPercentProgress < 0.0001)
                        setUICurrentStatus(UI_STATE.PHASE2_INIT.value);
                    else
                        setUICurrentStatus(UI_STATE.PHASE2_RUN.value);
            }
            setP1Progress(phaseOnePercentProgress);
            setP2Progress(phaseTwoPercentProgress);
            setDeepLearningError(deepLearningError);
            break;
        case API_AI_STATE.STOPPED.value:
            setUICurrentStatus(UI_STATE.STOPPED.value)
            break;
        case API_AI_STATE.COMPLETED.value:
            setUICurrentStatus(UI_STATE.COMPLETED.value);
            break;
        case API_AI_STATE.ERROR.value:
            setUICurrentStatus(UI_STATE.ERROR.value);
            break;
        default:
            setUICurrentStatus(UI_STATE.LISTENING_MODE.value);
            break;
    }
    ai_status_last = status;
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
                setUICurrentStatus(UI_STATE.PHASE1_INIT.value);
            } else {
                msgAlertProgressBar(ALERT.DANGER.value, 'Training cannot start! code error ' + statusCode);
                setUICurrentStatus(UI_STATE.ERROR.value);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(UI_STATE.ERROR.value);
            msgAlertProgressBar(ALERT.DANGER.value, 'Unexpected error occurred during start training');
        }
    });
}

function trainingStatusCall() {
    jQuery.ajax({
        url: './dynamic/trainingStatusAI.php',
        type: 'GET',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            try {
                var jsonData = JSON.parse(response);
                if (jsonData['api_status']['code'] === 200) {
                    setStateResponse(jsonData);
                } else {
                    setUICurrentStatus(UI_STATE.ERROR.value);
                    msgAlertProgressBar(ALERT.DANGER.value, 'An error has occurred while trying to get the AI\'s status');
                }
            } catch (e) {
                setUICurrentStatus(UI_STATE.ERROR.value);
                msgAlertProgressBar(ALERT.DANGER.value, 'Unable to query AI training status');
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(UI_STATE.ERROR.value);
            msgAlertProgressBar(ALERT.DANGER.value, 'Cannot contact server to query AI status');
        }
    });
}

function trainingRestart() {
    disableButtonUploadTextFile(true);
    startPollForStatus();

    phaseOneReset();
    phaseTwoReset();
    hideTrainingBar(true);
    hideChart(true);

    disableRestartBoxButton();
    setUICurrentStatus(UI_STATE.FILE_UPLOADED.value);

    disableButtonUploadTextFile(false);
}

function getUICurrentStatus() {
    var result = document.getElementById('training-status').value;
    setUICurrentStatus(UI_STATE.LISTENING_MODE.value); // listening mode
    return result;
}

function setUICurrentStatus(status) {
    document.getElementById('training-status').value = status;
}

function getP1Progress() {
    return document.getElementById('training-progress-phase1').value;
}

function setP1Progress(progress) {
    document.getElementById('training-progress-phase1').value = progress;
}

function getP2Progress() {
    return document.getElementById('training-progress-phase2').value;
}

function setP2Progress(progress) {
    document.getElementById('training-progress-phase2').value = progress;
}

function getDeepLearningError() {
    return document.getElementById('training-error').value;
}

function setDeepLearningError(error) {
    document.getElementById('training-error').value = error;
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

function showAlertMessage(aiStatus){
    switch(aiStatus){
        case API_AI_STATE.UNDEFINED.value:
            msgAlertProgressBar(ALERT.BASIC.value, 'Training not started. Please upload training data.');
            break;
        case API_AI_STATE.QUEUED.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. Please wait. The process is queued.');
            break;
        case API_AI_STATE.READY_TO_TRAIN.value:
            msgAlertProgressBar(ALERT.BASIC.value, 'Training not started.');
            break;
        case UI_TRAINING_STATE.PHASE1_INIT.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialization Phase one. Please wait.');
            break;
        case UI_TRAINING_STATE.PHASE1_RUN.value:
            msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase one in progress...');
            break;
        case UI_TRAINING_STATE.PHASE1_END.value:
            break;
        case UI_TRAINING_STATE.PHASE2_INIT.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialization Phase two may take one minute. Please wait.');
            break;
        case UI_TRAINING_STATE.PHASE2_RUN.value:
            msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase two in progress...');
            break;
        case API_AI_STATE.STOPPED.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Training stopped. Please restart training.');
            break;
        case API_AI_STATE.COMPLETED.value:
            msgAlertProgressBar(ALERT.SUCCESS.value, 'Training completed.');
            break;
        case API_AI_STATE.ERROR.value:
            msgAlertProgressBar(ALERT.DANGER.value, 'An error has occurred.');
            break;
        default:
            msgAlertProgressBar(ALERT.DANGER.value, 'State error.');
    }
}

function showAlertMessageFromUI(status){
    switch(status){
        case UI_STATE.FILE_UPLOADED.value:
            msgAlertProgressBar(ALERT.BASIC.value, 'Training not started.');
            break;
        case UI_STATE.PHASE1_INIT.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. Please wait.');
            break;
        case UI_STATE.PHASE1_QUEUE.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialising. Please wait. The process is queued.');
            break;
        case UI_STATE.PHASE1_RUN.value:
            msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase one in progress...');
            break;
        case UI_STATE.PHASE2_INIT.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Initialization Phase two may take one minute. Please wait.');
            break;
        case UI_STATE.PHASE2_RUN.value:
            msgAlertProgressBar(ALERT.PRIMARY.value, 'Phase two in progress...');
            break;
        case UI_STATE.STOPPED.value:
            msgAlertProgressBar(ALERT.WARNING.value, 'Training stopped. Please restart training.');
            break;
        case UI_STATE.COMPLETED.value:
            msgAlertProgressBar(ALERT.SUCCESS.value, 'Training completed.');
            break;
        default:
    }
}

function getErrorPercentProgress(progress){
    var percentProgress = progress * 100.0;
    if (percentProgress < 0.0) {
        percentProgress = 0.0;
    }
    if (percentProgress > 100.0) {
        percentProgress = 100.0;
    }
    return Math.floor(percentProgress);
}

function justStopped() {
    if (document.getElementById('containerMsgWarningAlertTrainingInfo') !== null)
        return true;
    else
        return false;
}

function phaseOneReset() {
    document.getElementById('progress-upload-file').style.width = '0%';
    document.getElementById('status-badge-upload').innerHTML = '0%';
}

function phaseTwoReset() {
    document.getElementById('progress-training-file').style.width = '0%';
    document.getElementById('status-badge-training').innerHTML = '0%';
}

function phaseOneFlashing(flag) {
    if (flag) {
        document.getElementById('status-upload-file').innerText = 'initialising';
        document.getElementById('status-upload-file').setAttribute('class', 'text-center flashing');
    } else {
        document.getElementById('status-upload-file').innerText = 'phase 1';
        document.getElementById('status-upload-file').setAttribute('class', 'text-center');
    }
}

function phaseTwoFlashing(flag) {
    if (flag) {
        document.getElementById('status-training-file').innerText = 'initialising';
        document.getElementById('status-training-file').setAttribute('class', 'text-center flashing');
    } else {
        document.getElementById('status-training-file').innerText = 'phase 2';
        document.getElementById('status-training-file').setAttribute('class', 'text-center');
    }
}

function phaseOneStriped(flag) {
    if (flag)
        document.getElementById('progress-upload-file-action').className = 'progress progress-xs progress-striped active';
    else
        document.getElementById('progress-upload-file-action').className = 'progress progress-xs active';
}

function phaseTwoStriped(flag) {
    if (flag)
        document.getElementById('progress-training-file-action').className = 'progress progress-xs progress-striped active';
    else
        document.getElementById('progress-training-file-action').className = 'progress progress-xs active';
}

function phaseOneQueue() {
    document.getElementById('status-upload-file').innerText = 'queued';
    document.getElementById('status-upload-file').setAttribute('class', 'text-center');
}

function phaseTwoQueue() {
    document.getElementById('status-training-file').innerText = 'queued';
    document.getElementById('status-training-file').setAttribute('class', 'text-center');
}

function hidePreTrainingBar(state) {
    $('#pretrainingbar').prop('hidden', state);
}

function hideTrainingBar(state) {
    $('#trainingbar').prop('hidden', state);
}

function hideChart(state) {
    $('#chart-details').prop('hidden', state);
    $('#chart-details-footer').prop('hidden', state);
}

function updateDeepLearningError(error){
    document.getElementById("show-error").innerText = error;
}

function phaseOneUpdate(progress) {
    document.getElementById("progress-upload-file").setAttribute('value', progress);
    document.getElementById("progress-upload-file").style.width = (parseInt(progress)) + '%';
    document.getElementById('status-badge-upload').innerHTML = parseInt(progress) + '%';
}

function phaseTwoUpdate(progress) {
    document.getElementById("progress-training-file").setAttribute('value', progress);
    document.getElementById("progress-training-file").style.width = (parseInt(progress)) + '%';
    document.getElementById('status-badge-training').innerHTML = parseInt(progress) + '%';
}

function phaseOneMaxValue() {
    document.getElementById('progress-upload-file').style.width = '100%';
    document.getElementById('status-badge-upload').innerHTML = '100%';
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

function hideRestartBox() {
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        element.parentNode.removeChild(element);
    }
}

function disableRestartBoxButton(state) {
    var element = document.getElementById('containerMsgWarningAlertTrainingInfo');
    if (element !== null) {
        if (state) {
            document.getElementById('restart-button').setAttribute('onClick', '');
            document.getElementById('restart-button').setAttribute("disabled", "disabled");
        }
        else {
            document.getElementById('restart-button').setAttribute('onClick', 'trainingRestart()');
            document.getElementById('restart-button').setAttribute("disabled", "enabled");
        }

    }
}

$("#collapseVideoTutorialTraining").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});

$( document ).ready(function() {
    initializePretrainingMonitor(aiStatus);
    initializeTrainingMonitor(aiStatus);
    initializeAlertMessage(aiStatus);
    initializeTrainingConsole(aiStatus);
});