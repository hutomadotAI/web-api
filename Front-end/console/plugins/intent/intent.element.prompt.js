document.getElementById("btnAddIntentPrompt").addEventListener("click", addIntentPrompt);

function checkInputPromptCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateIntentPrompt())
            addIntentPrompt();
    }
    else {
        activeButtonCreateIntentPrompt();
    }
}

function activeButtonCreateIntentPrompt() {
    var limitTextInputSize = 50;
    switch (limitText($("#intent-prompt"), limitTextInputSize)) {
        case -1:
            $("#btnAddIntentPrompt").prop("disabled", true);
            return false;
        case 0:
            msgAlertIntentPrompt(0, 'You can add intent prompts.');
            $("#btnAddIntentPrompt").prop("disabled", false);
            return true;
        case 1:
            msgAlertIntentPrompt(1, 'The intent prompt is too long!');
            $("#btnAddIntentPrompt").prop("disabled", true);
            return false
        default:
            $("#btnAddIntentPrompt").prop("disabled", true);
    }
    return false;
}

function addIntentPrompt() {
    $(this).prop("disabled", true);

    if (inputValidation($("#intent-prompt").val(), 'intent_prompt')) {
        msgAlertIntentPrompt(2, 'The intent prompt need contain only the following: BLA BLA BLA BLA character');
        return;
    }

    var prompts = [];
    var elements = document.getElementsByName('intent-prompt-row');
    for (var i = 0; i < elements.length; i++) {
        prompts.push(elements[i].value);
    }

    if(isNameExists($("#intent-prompt").val(),prompts)){
        msgAlertIntentPrompt(2, 'Two identical intent prompts are not allowed. Please choose a different name.');
        return;
    }

    var element = document.getElementById('intent-prompt');
    var value = $(element).val();
    var parent = document.getElementById('prompts-list');
    document.getElementById('intent-prompt').value = '';
    createNewPromptRow(value, parent);
    msgAlertIntentPrompt(0,'You can add additional an user expression');
}

function createNewPromptRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="promptOnMouseInRow(this)" onmouseout="promptOnMouseOutRow(this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-10" id="obj-prompt">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-tag text-gray"></i>');
    
    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="intent-prompt-row" name="intent-prompt-row"  style="background-color: #404446;" value="' + value + '" placeholder="' + value + '">');
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

    checkListPromptSize();
}

function checkListPromptSize() {
    if (document.getElementById('prompts-list').childElementCount > 0)
        $("#btnAddIntentPrompt").prop("disabled", false);
    else
        $("#btnAddIntentPrompt").prop("disabled", true);
}

function deleteRowPrompt(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent);
}

function promptOnMouseInRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function promptOnMouseOutRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

$(document).ready(function () {
    if (typeof intent['prompt'] == "undefined" || !(intent['prompt'] instanceof Array))
        return;

    var list_prompts = intent['prompt'];
    for (var x in list_prompts) {
        var value = list_prompts[x];
        var parent = document.getElementById('prompts-list');
        document.getElementById('intent-prompt').value = '';
        createNewPromptRow(value, parent);
    }
});