var variables = [];
var ID_pool;

function getMultipleElementValues(elementName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(addEscapeCharacter(elements[i].value));
    }
    return values;
}

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

function addEscapeCharacter(value) {
    return value.replace(/,/g, "||#44;");
}

function removeEscapeCharacter(value) {
    return value.replace(/\|\|#44;/g, ",");
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
        var prompts_split = list_prompt.split(',');

        if (list_prompt === '' || prompts_split.length === 0) {
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

        var labelElement = node.children[i].children[4].children[0].children[0].children[0];
        if (labelElement.value !== labelElement.getAttribute('placeholder')) {
            labelElement.setAttribute('placeholder', labelElement.value);
        }
        v['label'] = labelElement.getAttribute('placeholder');

        variables.push(v);
    }

    if (!isDataChanged()) {
        msgAlertIntentElement(ALERT.PRIMARY.value, 'No data changed');
        return false;
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
            variables: variables,
            webhook: webhook
        },
        type: 'POST',
        success: function (response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    msgAlertIntentElement(ALERT.PRIMARY.value, 'Intent saved');
                    enableSaving(false);
                    createWarningIntentAlert(INTENT_ACTION.SAVE_INTENT.value);
                    break;
                case 400: // fallthrough
                case 500: // fallthrough
                default:
                    msgAlertIntentElement(ALERT.DANGER.value, JSONdata['status']['info']);
                    break;
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

$('#boxPrompts').on('show.bs.modal', function (e) {
    var rowElement = $(e.relatedTarget).parent().parent().parent();
    cleanupromptDialogbox();
    loadPromptsForEntity(rowElement, e.currentTarget);
});

