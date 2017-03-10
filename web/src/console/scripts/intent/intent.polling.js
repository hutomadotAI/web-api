function createWarningIntentAlert(intent_action) {
    var wHTML = '';
    wHTML += ('<div class="box flat no-padding no-shadow no-margin">');
    wHTML += ('<div class="alert alert-dismissable flat alert-warning" id="containerWarningIntentAlert">');
    wHTML += ('<button type="button" class="close" id="btnRestart" data-dismiss="alert" aria-hidden="true" value="'+intent_action+'">×</button>');
    wHTML += ('<span id="msgAlertWarningIntentAlert">');
    wHTML += ('<dd class="text-center">');
    wHTML += ('Training must be restarted to incorporate your new changes.');
    wHTML += ('</dd>');
    wHTML += ('<p></p>');
    wHTML += ('<dt class="text-center">');
    wHTML += ('<button class="btn btn-primary btn-md center-block flat" id="restart-button" onclick="restartTraining();"> <b>Restart Training</b></button>');
    wHTML += ('</dt>');

    wHTML += ('</span>');
    wHTML += ('</div>');
    wHTML += ('<div class="overlay dark" id="alert-overlay" style="display: none;">');
    wHTML += ('<i class="fa fa-refresh fa-spin"></i>');
    wHTML += ('</div>');
    wHTML += ('</div>');

    var parent = document.getElementById('intentElementBox');
    parent.innerHTML = wHTML;
}

function removeWarningIntentAlert() {
    var element = document.getElementById('containerWarningIntentAlert');
    if (element !== null) {
        hideOverlay(true);
        element.parentNode.removeChild(element);
    }
}
function getIntentAction(){
    return JSON.parse(document.getElementById('btnRestart').value);
}

function hideOverlay(state){
    document.getElementById('alert-overlay').style.display = (state)?'none':'';
}

function startTraining(intent_action) {
    jQuery.ajax({
        url: './dynamic/trainingStart.php',
        type: 'GET',
        dataType: 'json',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            if (response['status']['code'] == 200 ) {
                if ( getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
                    msgAlertIntent(ALERT.BASIC.value, 'Create an Intent to trigger your own business logic.');
                else {
                    deactiveSaveButton(false);
                    msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
                }
                removeWarningIntentAlert();
            }
            else {
                if ( getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
                    msgAlertIntent(ALERT.DANGER.value, 'Could not start training.');
                else {
                    msgAlertIntentElement(ALERT.DANGER.value, 'Could not start training.');
                    deactiveSaveButton(false);
                }
                deactiveRestartButton(false);
                hideOverlay(true);
            }

        },
        error: function (xhr, ajaxOptions, thrownError) {
            if ( getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
                msgAlertIntent(ALERT.DANGER.value, 'Unexpected error occurred. Could not start training.');
            else {
                msgAlertIntentElement(ALERT.DANGER.value, 'Unexpected error occurred. Could not start training.');
                deactiveSaveButton(false);
            }
            deactiveRestartButton(false);
            hideOverlay(true);
        }
    });
}

function restartTraining() {
    deactiveRestartButton(true);
    if ( getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
        msgAlertIntent(ALERT.WARNING.value, 'Please wait...');
    else {
        deactiveSaveButton(true);
        msgAlertIntentElement(ALERT.WARNING.value, 'Please wait...');
    }
    hideOverlay(false);
    startPollForStatus();
}

function botStatusCall() {
    jQuery.ajax({
        url: './dynamic/trainingStatusAI.php',
        type: 'GET',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            var jsonData = JSON.parse(response);
            if (jsonData['api_status']['code'] === 200) {
                setBotStatus(jsonData["ai_status"]);
            } else {
                if (getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
                    msgAlertIntent(ALERT.DANGER.value, 'An error has occurred while trying to get the Bot\'s status.');
                else
                    msgAlertIntentElement(ALERT.DANGER.value, 'An error has occurred while trying to get the Bot\'s status.');
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (getIntentAction() == INTENT_ACTION.DELETE_INTENT.value)
                msgAlertIntent(ALERT.DANGER.value, 'Cannot contact server.');
            else
                msgAlertIntentElement(ALERT.DANGER.value, 'Cannot contact server.');
        }
    });
}

function pollStatus() {
    botStatusCall();
    isBotStopped();
}

function startPollForStatus() {
    ID_pool = setInterval(pollStatus, 1000);
}

function stopPollForStatus() {
    clearInterval(ID_pool);
}

function isBotStopped() {
    var status = document.getElementById('bot-status').value;
    setBotStatus(UI_STATE.LISTENING_MODE.value); // listening mode only for UI polling

    if( status == API_AI_STATE.STOPPED.value) {
        stopPollForStatus();
        startTraining();
    }
}

function setBotStatus(status) {
    document.getElementById('bot-status').value = status;
}

function deactiveRestartButton(state) {
    document.getElementById('restart-button').disabled = state;
}

function deactiveSaveButton(state) {
    document.getElementById('btnSaveEntity').disabled = state;
}