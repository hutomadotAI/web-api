function checkExpressionCode(element, key) {
    if (key === 13) {
        if( activeButtonCreateUserExpression())
            addUserExpression();
    }
    else {
        activeButtonCreateUserExpression();
    }
}

function checkInputPromptCode(element, key) {
    if (key === 13) {
        if (activeButtonCreateIntentPrompt())
            addIntentPrompt();
    }
    else {
        activeButtonCreateIntentPrompt();
    }
}

function checkIntentResponseCode(element, key) {
    if (key === 13) {
        if( activeButtonCreateIntentResponse())
            addIntentResponse();
    }
    else {
        activeButtonCreateIntentResponse();
    }
}

function activeButtonCreateUserExpression() {
    var limitTextInputSize = 250;
    msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
    switch (limitText($("#user-expression"), limitTextInputSize)) {
        case 0:
            msgAlertUserExpression(ALERT.BASIC.value, ' Give the bot examples of how a user would express this intent.');
            return true;
        case 1:
            msgAlertUserExpression(ALERT.WARNING.value, 'User expression is too long!');
            return false
        default:
    }
    return false;
}

function addUserExpression() {
    if (isInputInvalid($("#user-expression").val(), 'user_expression')) {
        msgAlertUserExpression(ALERT.DANGER.value, 'The user expression cannot contain invalid characters.');
        return;
    }

    var expressions = [];
    var elements = document.getElementsByName('user-expression-row');
    for (var i = 0; i < elements.length; i++) {
        expressions.push(elements[i].value);
    }

    if(isNameExists($("#user-expression").val(),expressions)){
        msgAlertUserExpression(ALERT.DANGER.value, 'That user expression already exists. Please choose a different expression.');
        return;
    }

    var element = document.getElementById('user-expression');
    var value = $(element).val();
    var parent = document.getElementById('userexpression-list');
    document.getElementById('user-expression').value = '';
    createNewUsersayRow(value, parent);
    msgAlertUserExpression(ALERT.BASIC.value,' Give the bot examples of how a user would express this intent.');
    enableSaving(true);
}

function createNewUsersayRow(value, parent) {
    $.get('templates/intent_value_expression.mustache', function (template) {
        var newNode = document.createElement('div');
        newNode.setAttribute('class', 'col-xs-12');
        newNode.innerHTML = Mustache.render(template, {
            expression: value.replace(/"/g, '&quot;')
        });
        parent.insertBefore(newNode, parent.firstChild);
    });
}

function deleteUserExpression(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent);
    enableSaving(true);
}

function expressionOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function expressionOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function activeButtonCreateIntentResponse() {
    var limitTextInputSize = 250;
    msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what actions should be taken by your business logic.');
    switch (limitText($("#intent-response"), limitTextInputSize)) {
        case 0:
            msgAlertIntentResponse(ALERT.BASIC.value, ' Give the bot examples of how it should respond to a user\'s intent.');
            return true;
        case 1:
            msgAlertIntentResponse(ALERT.WARNING.value, 'Intent\'s response is too long!');
            return false
        default:
    }
    return false;
}

function addIntentResponse() {
    if (isInputInvalid($("#intent-response").val(), 'intent_response')) {
        msgAlertIntentResponse(ALERT.DANGER.value, 'The intent response contains invalid characters.');
        return;
    }

    var responses = [];
    var elements = document.getElementsByName('intent-response-row');
    for (var i = 0; i < elements.length; i++) {
        responses.push(elements[i].value);
    }

    if(isNameExists($("#intent-response").val(),responses)){
        msgAlertIntentResponse(ALERT.DANGER.value, 'Response already exists. Please add a different response.');
        return;
    }

    var element = document.getElementById('intent-response');
    var value = $(element).val();
    var parent = document.getElementById('intentresponse-list');
    document.getElementById('intent-response').value = '';
    createNewIntentResponseRow(value, parent);
    msgAlertIntentResponse(ALERT.BASIC.value,'Give the bot examples of how it should respond to a user\'s intent.');
    enableSaving(true);
}

function createNewIntentResponseRow(value, parent) {
    $.get('templates/intent_value_response.mustache', function (template) {
        var newNode = document.createElement('div');
        newNode.setAttribute('class', 'col-xs-12');
        newNode.innerHTML = Mustache.render(template, {
            response: value.replace(/"/g, '&quot;')
        });
        parent.insertBefore(newNode, parent.firstChild);
    });
}

function deleteIntentResponse(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent)
    enableSaving(true);
}

function responseOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function responseOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function initializeExpressions() {
    if (typeof intent['user_says'] !== "undefined" && (intent['user_says'] instanceof Array)) {
        var list_expressions = intent['user_says'];
        for (var x in list_expressions) {
            var value = list_expressions[x];
            var parent = document.getElementById('userexpression-list');
            document.getElementById('user-expression').value = '';
            createNewUsersayRow(value, parent);
        }
    }
}

function initializeResponses() {
    if (typeof intent['responses'] !== "undefined" && (intent['responses'] instanceof Array)) {
        var list_responses = intent['responses'];
        for (var x in list_responses) {
            var value = list_responses[x];
            var parent = document.getElementById('intentresponse-list');
            document.getElementById('intent-response').value = '';
            createNewIntentResponseRow(htmlEncode(value), parent);
        }
    }
}


function activeButtonCreateIntentPrompt() {
    var limitTextInputSize = 250;
    switch (limitText($("#intent-prompt"), limitTextInputSize)) {
        case 0:
            msgAlertIntentPrompt(ALERT.BASIC.value, 'Please enter one or more prompts.');
            return true;
        case 1:
            msgAlertIntentPrompt(ALERT.WARNING.value, 'Intent prompt is too long!');
            return false;
        default:
    }
    return false;
}

function addIntentPrompt() {
    if (isInputInvalid($("#intent-prompt").val(), 'intent_prompt')) {
        msgAlertIntentPrompt(ALERT.DANGER.value, 'The intent prompt contains invalid characters.');
        return;
    }

    var prompts = getMultipleElementValues('intent-prompt-row', 'value');

    if (isNameExists($("#intent-prompt").val(), prompts)) {
        msgAlertIntentPrompt(ALERT.DANGER.value, 'Duplicate intent prompt.');
        return;
    }

    var element = document.getElementById('intent-prompt');
    var value = $(element).val();
    var parent = document.getElementById('prompts-list');
    document.getElementById('intent-prompt').value = '';
    createNewPromptRow(value, parent);
    msgAlertIntentPrompt(ALERT.BASIC.value, 'Enter one or more prompts.');
    resetMsgAlertIntentVariable();
    $(this).prop("disabled", false);
    enableSaving(true);
}


function createNewPromptRow(value, parent) {
    $.get('templates/intent_value_prompt.mustache', function (template) {
        var newNode = document.createElement('div');
        newNode.setAttribute('class', 'col-xs-12');
        newNode.innerHTML = Mustache.render(template, {
            prompt: value.replace(/"/g, '&quot;')
        });
        parent.insertBefore(newNode, parent.firstChild);
    });
}

function deleteRowPrompt(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem = $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent);
    enableSaving(true);
}

function promptOnMouseInRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function promptOnMouseOutRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function setNewListPrompts() {
    var intentNewPromptList = getMultipleElementValues('intent-prompt-row');
    var entityRowNumber = document.getElementById('var_row').value;
    if (entityRowNumber === null) {
        return;
    }
    var entityRow = document.getElementById('parameter-list').children[entityRowNumber];
    var node_prompt = entityRow.children[2].children[0].children[0];
    node_prompt.setAttribute('data-prompts', encodeStringArrayAsCSString(intentNewPromptList));
    if (intentNewPromptList.length > 0) {
        node_prompt.setAttribute('placeholder', ' ... ');
    } else {
        node_prompt.setAttribute('placeholder', 'click to enter');
    }
}

function loadPromptsForEntity(rowElement, targetModal) {
    // Find the position of the row
    var rowPosition = -1;
    for (var i = 0; i < rowElement.parent().children().length; i++) {
        if (rowElement.parent().children()[i] === rowElement[0]) {
            rowPosition = i;
            break;
        }
    }
    if (rowPosition === -1) { // Should not happen
        return;
    }

    $(targetModal).find('input[name="var_row"]').val(rowPosition);

    //send to modal current entity name selected from first node in the current variables row selected
    var node_entity = rowElement.children().children().children();
    var entityNode = $(node_entity).find("ul").find("li.selected");
    var curr_entity = entityNode.text();
    $(targetModal).find('input[name="curr_entity"]').val(curr_entity);

    //send to modal current intent store in data-intent html
    var curr_intent = document.getElementById('intent-name').value;
    $(targetModal).find('input[name="curr_intent"]').val(curr_intent);

    //send to modal current n prompt value or placeholder if is not changed from second node in the current variables row selected
    var node_n_prompts = rowElement.children().eq(1).children().children();
    var curr_n_prompts;
    if (node_n_prompts.val() === '' || node_n_prompts.val() === 'n° prompt') {
        curr_n_prompts = node_n_prompts.attr('placeholder');
    } else {
        curr_n_prompts = node_n_prompts.val();
    }
    $(targetModal).find('input[name="curr_n_prompts"]').val(curr_n_prompts);

    var promptsNode = rowElement.children().eq(2).children().children();
    var promptsListAsString = promptsNode.context.getAttribute('data-prompts');

    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    var parent = document.getElementById('prompts-list');
    var prompts = decodeCSStringAsArray(promptsListAsString);
    if (promptsListAsString !== '') {
        for (var j = 0; j < prompts.length; j++) {
            createNewPromptRow(prompts[j], parent);
        }
    } else {
        var inputNode = rowElement.children().children().eq(2).children()[0];
        inputNode.setAttribute('placeholder', 'click to enter');
    }
}


function updateWebhookSaving(){
    enableSaving(true);
    msgAlertWebHook(ALERT.BASIC.value, 'Provide the WebHook endpoint.');
}

$(document).ready(function () {
    document.getElementById("btnAddExpression").addEventListener("click", addUserExpression);
    document.getElementById("btnAddIntentResponse").addEventListener("click", addIntentResponse);
    document.getElementById("btnAddIntentPrompt").addEventListener("click", addIntentPrompt);
    document.getElementById("btnModelPromptClose").addEventListener("click", setNewListPrompts);

    initializeExpressions();
    initializeResponses();

    if (typeof intent['webhook'] !== "undefined") {
        $('#webhook').val(intent['webhook']['endpoint']);
    }
});
