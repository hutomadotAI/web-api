function createWarningIntentAlert(intent_action) {
    $.get('templates/intent_value_restart_training_warning.mustache', function (template) {
        $('#intentElementBox').html(Mustache.render(template, {}));
    });
}

function removeWarningIntentAlert() {
    var element = document.getElementById('containerWarningIntentAlert');
    if (element !== null) {
        hideOverlay(true);
        element.parentNode.removeChild(element);
    }
}
function getIntentAction() {
    return JSON.parse(document.getElementById('btnRestart').value);
}

function hideOverlay(state) {
    document.getElementById('alert-overlay').style.display = (state) ? 'none' : '';
}
function showAlertMessage(code, intent_action) {
    switch (intent_action) {
        case INTENT_ACTION.DELETE_INTENT.value:
            if (code === 200) {
                msgAlertIntent(ALERT.BASIC.value, 'Create an Intent to trigger your own business logic.');
                removeWarningIntentAlert();
            }
            else {
                msgAlertIntent(ALERT.DANGER.value, 'Could not start training.');
                deactiveRestartButton(false);
                hideOverlay(true);
            }
            break;
        case INTENT_ACTION.SAVE_INTENT.value:
            if (code === 200) {
                msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
                removeWarningIntentAlert();
            } else {
                msgAlertIntentElement(ALERT.DANGER.value, 'Could not start training.');
                deactiveRestartButton(false);
                hideOverlay(true);
            }
            deactiveSaveButton(false);
            break;
        default:
    }
}

function startTraining() {
    var ERROR_MESSAGE = 'Unexpected error occurred. Could not start training.';
    jQuery.ajax({
        url: './proxy/trainingStart.php',
        type: 'GET',
        dataType: 'json',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            showAlertMessage(response['status']['code'], getIntentAction());
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (getIntentAction() === INTENT_ACTION.DELETE_INTENT.value)
                msgAlertIntent(ALERT.DANGER.value, ERROR_MESSAGE);
            else {
                msgAlertIntentElement(ALERT.DANGER.value, ERROR_MESSAGE);
                deactiveSaveButton(false);
            }
            deactiveRestartButton(false);
            hideOverlay(true);
        }
    });
}

function updateTraining() {
    var ERROR_MESSAGE = 'Unexpected error occurred. Could not start training.';
    jQuery.ajax({
        url: './proxy/trainingUpdate.php',
        type: 'GET',
        dataType: 'json',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (xhr) {
            if (xhr.status !== null) {
                if (xhr.status.code === 200) {
                    startTraining();
                    return;
                } else {
                    msgAlertIntentElement(ALERT.DANGER.value, xhr.status.info);
                }
            } else {
                msgAlertIntentElement(ALERT.DANGER.value, ERROR_MESSAGE);
            }
            deactiveRestartButton(false);
            hideOverlay(true);
        },
        error: function () {
            if (getIntentAction() === INTENT_ACTION.DELETE_INTENT.value)
                msgAlertIntent(ALERT.DANGER.value, ERROR_MESSAGE);
            else {
                msgAlertIntentElement(ALERT.DANGER.value, ERROR_MESSAGE);
                deactiveSaveButton(false);
            }
            deactiveRestartButton(false);
            hideOverlay(true);
        }
    });
}

function restartTraining() {
    deactiveRestartButton(true);
    if (getIntentAction() === INTENT_ACTION.DELETE_INTENT.value)
        msgAlertIntent(ALERT.WARNING.value, 'Please wait...');
    else {
        deactiveSaveButton(true);
        msgAlertIntentElement(ALERT.WARNING.value, 'Please wait...');
    }
    updateTraining();
    hideOverlay(false);
    startPollForStatus();
}

function botStatusCall() {
    jQuery.ajax({
        url: './proxy/trainingStatusAI.php',
        type: 'GET',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            var jsonData = JSON.parse(response);
            var msg = 'An error has occurred while trying to get the bot\'s status.';
            if (jsonData['api_status']['code'] === 200)
                setBotStatus(jsonData["ai_status"]);
            else {
                if (getIntentAction() === INTENT_ACTION.DELETE_INTENT.value)
                    msgAlertIntent(ALERT.DANGER.value, msg);
                else
                    msgAlertIntentElement(ALERT.DANGER.value, msg);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (getIntentAction() === INTENT_ACTION.DELETE_INTENT.value)
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

    if (status === API_AI_STATE.STOPPED.value ||
        status === API_AI_STATE.READY_TO_TRAIN.value ||
        status === API_AI_STATE.COMPLETED.value) {
        stopPollForStatus();
        updateTraining();
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