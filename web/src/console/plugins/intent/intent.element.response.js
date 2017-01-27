document.getElementById("btnAddIntentResponse").addEventListener("click", addIntentResponse);

if (limitText($("#intent-response")) == 0)
    $("#addIntentResponse").prop("disabled", false);

function checkIntentResponseCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateIntentResponse())
            addIntentResponse();
    }
    else {
        activeButtonCreateIntentResponse();
    }
}

function activeButtonCreateIntentResponse() {
    var limitTextInputSize = 50;
    msgAlertIntentElement(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
    switch (limitText($("#intent-response"), limitTextInputSize)) {
        case -1:
            $("#btnAddIntentResponse").prop("disabled", true);
            return false;
        case 0:
            msgAlertIntentResponse(ALERT.BASIC.value, ' Give the AI examples of how a user would response this intent.');
            $("#btnAddIntentResponse").prop("disabled", false);
            return true;
        case 1:
            msgAlertIntentResponse(ALERT.WARNING.value, 'The intent resposne is too long!');
            $("#btnAddIntentResponse").prop("disabled", true);
            return false
        default:
            $("#btnAddIntentResponse").prop("disabled", true);
    }
    return false;
}

function addIntentResponse() {
    $(this).prop("disabled", true);

    if (inputValidation($("#intent-response").val(), 'intent_response')) {
        msgAlertIntentResponse(ALERT.DANGER.value, 'The intent response need contain only the following: BLA BLA BLA BLA character');
        return;
    }

    var responses = [];
    var elements = document.getElementsByName('intent-response-row');
    for (var i = 0; i < elements.length; i++) {
        responses.push(elements[i].value);
    }

    if(isNameExists($("#intent-response").val(),responses)){
        msgAlertIntentResponse(ALERT.DANGER.value, 'Two identical intent responses are not allowed. Please choose a different name.');
        return;
    }

    var element = document.getElementById('intent-response');
    var value = $(element).val();
    var parent = document.getElementById('intentresponse-list');
    document.getElementById('intent-response').value = '';
    createNewIntentResponseRow(value, parent);
    msgAlertIntentResponse(ALERT.BASIC.value,' Give the AI examples of how a user would response this intent.');
}

function createNewIntentResponseRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="responseOnMouseIn (this)" onmouseout="responseOnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-9" id="obj-intentresponse">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-comments-o text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="intent-response-row" name="intent-response-row" style="padding-left: 35px;background-color: #404446; " value="' + value + '" placeholder="' + value + '">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3" id="btnIntentResponse" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');
    
    wHTML += ('<a data-toggle="modal" data-target="#deleteIntentResponse" style="padding-right:3px; cursor: pointer;" onClick="deleteIntentResponse(this)">');
    wHTML += ('<i class="fa fa-trash-o text-gray" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    checkListIntentResponseSize();
}

function checkListIntentResponseSize() {
    if (document.getElementById('intentresponse-list').childElementCount > 0)
        $("#btnAddIntentResponse").prop("disabled", false);
    else 
        $("#btnAddIntentResponse").prop("disabled", true);
}

function deleteIntentResponse(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent)
}

function responseOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function responseOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

$(document).ready(function () {
    if (typeof intent['responses'] == "undefined" || !(intent['responses'] instanceof Array))
        return;

    var list_responses = intent['responses'];
    for (var x in list_responses) {
        var value = list_responses[x];
        var parent = document.getElementById('intentresponse-list');
        document.getElementById('intent-response').value = '';
        createNewIntentResponseRow(value, parent);
    }
});