document.getElementById("btnAddIntentPrompt").addEventListener("click", addIntentPrompt);
document.getElementById("btnModelPromptClose").addEventListener("click", setNewListPrompts);

function checkInputPromptCode(element, key) {
    if (key === 13) {
        if (activeButtonCreateIntentPrompt())
            addIntentPrompt();
    }
    else {
        activeButtonCreateIntentPrompt();
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
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="promptOnMouseInRow(this)" onmouseout="promptOnMouseOutRow(this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-10" id="obj-prompt">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-comments text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="intent-prompt-row" name="intent-prompt-row"  style="background-color: #404446;" value="' + value + '" placeholder="' + value + '" onkeydown="enableSaving(true);">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" id="btnRowPrompt" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');

    wHTML += ('<a data-toggle="modal" data-target="#deleteRowPrompt" style="padding-right:3px;" onClick="deleteRowPrompt(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12 no-padding');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.appendChild(newNode);
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

function addSepatator(arr, len, separator) {
    for (var i = 0; i < len; i++)
        arr[i] += separator;
}

function setNewListPrompts() {
    var intentNewPromptList = getMultipleElementValues('intent-prompt-row');
    var entityRowNumber = document.getElementById('var_row').value;
    if (entityRowNumber === null) {
        return;
    }
    var entityRow = document.getElementById('parameter-list').children[entityRowNumber];
    var node_prompt = entityRow.children[2].children[0].children[0];
    node_prompt.setAttribute('data-prompts', intentNewPromptList);
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
    var prompts_split = promptsListAsString.split(',');
    if (promptsListAsString !== '') {
        for (var j = 0; j < prompts_split.length; j++) {
            var prompt = removeEscapeCharacter(prompts_split[j]);
            createNewPromptRow(prompt, parent);
        }
    } else {
        var inputNode = rowElement.children().children().eq(2).children()[0];
        inputNode.setAttribute('placeholder', 'click to enter');
    }
}

