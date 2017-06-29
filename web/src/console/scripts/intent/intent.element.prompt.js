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
    parent.insertBefore(newNode, parent.firstChild);
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
    var entity_selected = false;
    var curr_entity = document.getElementById('curr_entity').value;
    var intentNewPromptList = getMultipleElementValues('intent-prompt-row');
    var node = document.getElementById('parameter-list');
    var len = node.childNodes.length;

    if (curr_entity === '') {
        var first_node_prompt = node.children[0].children[2].children[0].children[0];
        first_node_prompt.setAttribute('data-prompts', intentNewPromptList);
        first_node_prompt.setAttribute('placeholder', ' ... ');
        return;
    }

    for (var i = 0; i < len; i++) {
        // be carefull - the node is tree for prompts list access variable->fieldvariable->textdiv->attribute
        var node_entity = node.children[i].children[0].children[0].children[0];
        var node_prompt = node.children[i].children[2].children[0].children[0];
        var elem = $(node_entity).find("ul").find("li.selected");
        if (elem.text() === curr_entity) {
            node_prompt.setAttribute('data-prompts', intentNewPromptList);

            var list_prompt = node_prompt.getAttribute('data-prompts');
            if (list_prompt !== '')
                node_prompt.setAttribute('placeholder', ' ... ');
            else
                node_prompt.setAttribute('placeholder', 'click to enter');
            entity_selected = true;
        }
    }
}

function loadPromptsForEntity(elem) {
    // Joining the madness...
    var inputNode = elem.context.parentNode.parentNode.parentNode.children[2].children[0].children[0];

    var parent = document.getElementById('prompts-list');
    var list_prompt = inputNode.getAttribute('data-prompts');
    var prompts_split = list_prompt.split(',');
    if (list_prompt !== '') {
        for (var j = 0; j < prompts_split.length; j++) {
            var prompt = removeEscapeCharacter(prompts_split[j]);
            createNewPromptRow(prompt, parent);
        }
    } else {
        node.setAttribute('placeholder', 'click to enter');
    }
}

