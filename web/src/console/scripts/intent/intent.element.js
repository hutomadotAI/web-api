var variables = [];
var ID_pool;

function getMultipleElementValues(elementName, attributeName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(addEscapeCharacter(elements[i].getAttribute(attributeName)));
    }
    return values;
}

function addEscapeCharacter(value) {
    return value.replace(/,/g, "||#44;");
}

function removeEscapeCharacter(value) {
    return value.replace(/\|\|#44;/g, ",");
}

function saveIntent() {
    $(this).prop("disabled", true);

    var intentName = document.getElementById('intent-name').value;
    var expressions = getMultipleElementValues('user-expression-row', 'placeholder');
    var responses = getMultipleElementValues('intent-response-row', 'placeholder');
    var variables = [];

    var hasErrors = false;
    if (expressions.length == 0) {
        msgAlertUserExpression(ALERT.DANGER.value, 'At least one user expression is required.');
        hasErrors = true;
    }
    if (responses.length == 0) {
        msgAlertIntentResponse(ALERT.DANGER.value, 'At least one response is required.');
        hasErrors = true;
    }
    if (hasErrors) {
        msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
        return false;
    }

    var node = document.getElementById('parameter-list');
    var len = node.childNodes.length;

    for (var i = 0; i < len; i++) {
        var v = {};

        //*** check validation entity name
        var node_entity = node.children[i].children[0].children[0].children[0];
        var elem = $(node_entity).find("ul").find("li.selected");
        if (elem.text() == '') {
            node.children[i].children[0].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Cannot save. Missing entity.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }

        v['entity_name'] = elem.text().replace(/[@]/g, "");

        //*** check validation n prompt
        var node_nprompt = node.children[i].children[1].children[0].children[0];

        if (node_nprompt.value != '' && node_nprompt.value !== 'undefined') {
            if (isInputInvalid(node_nprompt.value, 'intent_n_prompt')) {
                node.children[i].children[1].children[0].children[0].style.border = "thin dotted red";
                msgAlertIntentVariable(ALERT.DANGER.value, 'The number of prompts must be between 1 and 99.');
                msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
                return false;
            }
            node_nprompt.setAttribute('placeholder', node_nprompt.value);
        }

        if (node_nprompt.getAttribute('placeholder') == 'n° prompt') {
            node.children[i].children[1].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Cannot save. Missing n° prompt value.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }

        v['n_prompts'] = node_nprompt.getAttribute('placeholder');

        //*** check validation list prompts
        var node_prompt = node.children[i].children[2].children[0].children[0];
        var list_prompt = node_prompt.getAttribute('data-prompts');
        var prompts_split = list_prompt.split(',');

        if (list_prompt == '' || prompts_split.length == 0) {
            node.children[i].children[2].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Please add at least one prompt before saving.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }


        var promptsArray = [];
        for (var j = 0; j < prompts_split.length; j++)
            promptsArray.push(removeEscapeCharacter(prompts_split[j]));
        v['prompts'] = promptsArray;

        //*** check required checkbox
        var node_required = node.children[i].children[3].children[0].children[0].children[0];
        v['required'] = node_required.checked;

        variables.push(v);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveIntent").prop("disabled", true);
    resetMsgAlertIntentVariable();

    msgAlertIntentElement(ALERT.WARNING.value, 'saving...');
    $.ajax({
        url: './dynamic/updateIntent.php',
        data: {
            intent_name: intentName,
            intent_expressions: expressions,
            intent_responses: responses,
            variables: variables
        },
        type: 'POST',
        success: function (response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    msgAlertIntentElement(ALERT.PRIMARY.value, 'Intent saved!!');
                    if (trainingFile)
                        createWarningIntentAlert();
                    break;
                case 400:
                    msgAlertIntentElement(ALERT.DANGER.value, JSONdata['status']['info']);
                    break;
                case 500:
                    msgAlertIntentElement(ALERT.DANGER.value, JSONdata['status']['info']);
                    break;
                default:
                    msgAlertIntentElement(ALERT.DANGER.value, JSONdata['status']['info']);
            }
        },
        complete: function () {
            $("#btnSaveIntent").prop("disabled", false);
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            msgAlertIntentElement(ALERT.DANGER.value, 'Unexpected error occurred. Intent not saved!');
        }
    });
}

function createWarningIntentAlert() {
    var wHTML = '';
    wHTML += ('<div class="box flat no-padding no-shadow no-margin">');
    wHTML += ('<div class="alert alert-dismissable flat alert-warning" id="containerWarningIntentAlert">');
    wHTML += ('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
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

function startTraining() {
    jQuery.ajax({
        url: './dynamic/trainingStart.php',
        type: 'GET',
        dataType: 'json',
        processData: false,
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            if (response['status']['code'] == 200 ) {
                removeWarningIntentAlert();
                msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
            }
            else {
                deactiveRestartButton(false);
                hideOverlay(true);
                msgAlertIntentElement(ALERT.DANGER.value, 'Could not start training.');
            }

        },
        complete: function(){
            deactiveSaveButton(false);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            deactiveRestartButton(false);
            hideOverlay(true);
            msgAlertIntentElement(ALERT.DANGER.value, 'Unexpected error occurred. Could not start training.');
        }
    });
}

function restartTraining() {
    deactiveSaveButton(true);
    deactiveRestartButton(true);
    msgAlertIntentElement(ALERT.WARNING.value, 'Please wait...');
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
                updateWarningIntentAlertButton(false);
                msgAlertIntentElement(ALERT.DANGER.value, 'An error has occurred while trying to get the Bot\'s status.');
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            updateWarningIntentAlertButton(false);
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


function hideOverlay(state){
    if (state)
        document.getElementById('alert-overlay').style.display ='none';
    else
        document.getElementById('alert-overlay').style.display ='';
}


$('#boxPrompts').on('show.bs.modal', function (e) {
    var parent = $(e.relatedTarget).parent().parent().parent();

    //send to modal current entity name selected from first node in the current variables row selected
    var node_entity = parent.children().children().children();
    var elem = $(node_entity).find("ul").find("li.selected");
    var curr_entity = elem.text();
    $(e.currentTarget).find('input[name="curr_entity"]').val(curr_entity);

    //send to modal current intent store in data-intent html
    var curr_intent = document.getElementById('intent-name').value;
    $(e.currentTarget).find('input[name="curr_intent"]').val(curr_intent);

    //send to modal current n prompt value or placeholder if is not changed from second node in the current variables row selected
    var node_n_prompts = parent.children().eq(1).children().children();
    var curr_n_prompts;
    if (node_n_prompts.val() == '' || node_n_prompts.val() == 'n° prompt')
        curr_n_prompts = node_n_prompts.attr('placeholder');
    else
        curr_n_prompts = node_n_prompts.val();
    $(e.currentTarget).find('input[name="curr_n_prompts"]').val(curr_n_prompts);

    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    cleanupromptDialogbox();
    loadPromptsForEntity(curr_entity)
});

