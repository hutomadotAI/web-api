var variables = [];
var ID_pool;

function getWebHookValues() {
    var webhook = {};
    webhook['intent_name'] = document.getElementById('intent-name').value;
    webhook['endpoint'] = document.getElementById('webhook').value;
    webhook['enabled'] = !(webhook['endpoint'].trim()==="");
    return webhook;
}

function enableSaving(state){
    data_changed = state;
}

function isDataChanged(){
    return data_changed;
}

function saveIntent() {
    $(this).prop("disabled", true);

    var intentName = document.getElementById('intent-name').value;
    var expressions = getMultipleElementValues('user-expression-row');
    var responses = getMultipleElementValues('intent-response-row');
    var webhook = getWebHookValues();
    var variables = [];

    var hasErrors = false;
    if (expressions.length === 0) {
        msgAlertUserExpression(ALERT.DANGER.value, 'At least one user expression is required.');
        hasErrors = true;
    }
    if (responses.length === 0) {
        msgAlertIntentResponse(ALERT.DANGER.value, 'At least one response is required.');
        hasErrors = true;
    }
    if (webhook['enabled'] && isInputInvalid(webhook['endpoint'], 'webhook')){
        msgAlertWebHook(ALERT.DANGER.value, 'Please enter a valid URL.');
        hasErrors = true;
    }
    if (hasErrors) {
        msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
        return false;
    }

    var node = document.getElementById('parameter-list');
    var len = node.childNodes.length;
    var labelsMap = new Object();

    for (var i = 0; i < len; i++) {
        var v = {};

        //*** check validation entity name
        var node_entity = node.children[i].children[0].children[0].children[0];
        var elem = $(node_entity).find("ul").find("li.selected");
        if (elem.text() === '') {
            node.children[i].children[0].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Cannot save. Missing entity.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }

        v['entity_name'] = elem.text().replace(/[@]/g, "");

        //*** check validation n prompt
        var node_nprompt = node.children[i].children[1].children[0].children[0];

        if (node_nprompt.value !== '' && typeof node_nprompt.value !== 'undefined') {
            if (isInputInvalid(node_nprompt.value, 'intent_n_prompt')) {
                node.children[i].children[1].children[0].children[0].style.border = "thin dotted red";
                msgAlertIntentVariable(ALERT.DANGER.value, 'The number of prompts must be between 1 and 99.');
                msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
                return false;
            }
            node_nprompt.setAttribute('placeholder', node_nprompt.value);
        }

        if (node_nprompt.getAttribute('placeholder') === 'n° prompt') {
            node.children[i].children[1].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Cannot save. Missing n° prompt value.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }

        v['n_prompts'] = node_nprompt.getAttribute('placeholder');

        //*** check validation list prompts
        var node_prompt = node.children[i].children[2].children[0].children[0];
        var list_prompt = node_prompt.getAttribute('data-prompts');
        var prompts = decodeCSStringAsArray(list_prompt);

        if (list_prompt === '' || prompts.length === 0) {
            node.children[i].children[2].children[0].children[0].style.border = "thin dotted red";
            msgAlertIntentVariable(ALERT.DANGER.value, 'Please add at least one prompt before saving.');
            msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
            return false;
        }

        v['prompts'] = prompts;

        //*** check required checkbox
        var node_required = node.children[i].children[3].children[0].children[0].children[0];
        v['required'] = node_required.checked;

        var labelElement = node.children[i].children[4].children[0].children[0].children[0];
        v['label'] = labelElement.value;

        variables.push(v);

        var labelArray = [];
        if(labelsMap.hasOwnProperty(v['entity_name'])) {
            labelArray = labelsMap[v['entity_name']];
        }
        labelArray.push({label: v['label'].trim(), node: labelElement});
        labelsMap[v['entity_name']] = labelArray;
    }


    var usedLabels = new Object();
    for (var entityName in labelsMap) {
        if (labelsMap.hasOwnProperty(entityName)) {
            var labelArr = labelsMap[entityName];
            for (var x in labelArr) {
                var label = labelArr[x].label;
                if (label === "" || usedLabels.hasOwnProperty(label)) {
                    labelArr[x].node.style.border = "thin dotted red";
                    msgAlertIntentVariable(ALERT.DANGER.value, 'You need to provide a unique label for each entity.');
                    msgAlertIntentElement(ALERT.DANGER.value, 'Intent not saved: Please review the errors below.');
                    return false;
                }
                usedLabels[label] = true;
            }
        }
    }

    if (!isDataChanged()) {
        msgAlertIntentElement(ALERT.PRIMARY.value, 'No data changed');
        return false;
    }

    $("#btnSaveIntent").prop("disabled", true);
    resetMsgAlertIntentVariable();
    msgAlertIntentElement(ALERT.WARNING.value, 'saving...');

    saveIntentToApi(intentName, expressions, responses, variables, webhook);
}

function saveIntentToApi(intentName, expressions, responses, variables, webhook) {
    var prevCursor = document.body.style.cursor;
    var request = {
        url: './proxy/intentProxy.php',
        data: {
            intent_name: intentName,
            intent_expressions: expressions,
            intent_responses: responses,
            variables: variables,
            webhook: webhook
        },
        verb: 'PUT',
        onGenericError: function () {
            msgAlertIntentElement(ALERT.DANGER.value, "There was a problem saving the intent.");
        },
        onOK: function(response) {
            msgAlertIntentElement(ALERT.PRIMARY.value, 'Intent saved');
            enableSaving(false);
            createWarningIntentAlert(INTENT_ACTION.SAVE_INTENT.value);
        },
        onShowError: function (message) {
            msgAlertIntentElement(message);
        },
        onComplete: function () {
            document.body.style.cursor = prevCursor;
        }

    };
    document.body.style.cursor = 'wait';
    commonAjaxApiRequest(request);
}

$('#boxPrompts').on('show.bs.modal', function (e) {
    var rowElement = $(e.relatedTarget).parent().parent().parent();
    cleanupromptDialogbox();
    loadPromptsForEntity(rowElement, e.currentTarget);
});

window.onbeforeunload = function (e) {
    var e = e || window.event;
    // For IE and Firefox prior to version 4
    if (e && data_changed) {
        e.returnValue = 'Are you sure you want to leave this page?';
    }
    // For Safari
    if (data_changed) {
        return 'Are you sure you want to leave this page?';
    }
};