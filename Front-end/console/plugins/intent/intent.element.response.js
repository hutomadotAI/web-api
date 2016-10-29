/**
 * Created by Hutoma on 20/10/16.
 */
document.getElementById("btnAddIntentResponse").addEventListener("click", addIntentResponse);

function getMultipleElementValues(elementName, attributeName, startIndex) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = startIndex; i < elements.length; i++) {
        values.push(elements[i].getAttribute(attributeName));
    }
    return values;
}

function getMultipleTextElementValues(elementName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(elements[i].value);
    }
    return values;
}

function getMultipleCheckElementValues(elementName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(elements[i].checked);
    }
    return values;
}

function saveIntent() {
    var responses = getMultipleElementValues('intent-response', 'placeholder', 1);
    var expressions = getMultipleElementValues('user-expression', 'placeholder', 1);
    var intentName = document.getElementById('intent-name').value;
    var entityNames = getMultipleElementValues('action-entity', 'placeholder', 0);


    // TODO: we need to get a much better way of doing this - Bug460
    var prompts1 = getMultipleTextElementValues('action-prompts');
    var prompts2 = getMultipleElementValues('action-prompts', 'placeholder', 0);
    var prompts = [];
    for (var i = 0; i < prompts1.length; i++) {
        var promptsArray = [];
        if (prompts1[i] == '') {
            promptsArray.push(prompts2[i] == 'click to enter' ? prompts1[i] : prompts2[i]);
        } else {
            promptsArray.push(prompts1[i]);
        }
        prompts[i] = promptsArray;
    }

    var numberPrompts1 = getMultipleTextElementValues('action-nprompt');
    var numberPrompts2 = getMultipleElementValues('action-nprompt', 'placeholder', 0);
    var numberPrompts = [];
    for (i = 0; i < numberPrompts1.length; i++) {
        numberPrompts[i] = numberPrompts2[i] == 'n° prompt' ? numberPrompts1[i] : numberPrompts2[i];
    }

    var required = getMultipleCheckElementValues('action-required');
    var variables = [];
    for (i = 0; i < entityNames.length; i++) {
        if (prompts[i] == '' || numberPrompts[i] == '' || entityNames[i][0] != '@' || entityNames[i].length < 2) {
            containerMsgAlertIntentVariable(2, 'Please enter all the fields for entity ' +
                (entityNames[i][0] == '@' ? entityNames[i] : ('at row ' + (i + 1))));
            return;
        }

        var v = {};
        v['entity_name'] = entityNames[i][0] == '@' ? entityNames[i].substring(1, entityNames[0].length) : entityNames[i];
        v['prompts'] = prompts[i];
        v['n_prompts'] = numberPrompts[i] == '' ? 1 : numberPrompts[i];
        v['required'] = required[i];
        //v['value'] = '';
        variables.push(v);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveIntent").prop("disabled", true);
    resetMsgAlertIntentVariable();

    $.ajax({
        url: 'intentelement.php?intent=' + intentName,
        data: {
            intent_name: intentName, intent_prompts: expressions, intent_responses: responses,
            variables: variables
        },
        type: 'POST',
        /*error: function (xhr, ajaxOptions, thrownError) {
         alert(xhr.status + ' ' + thrownError);
         }*/
        success: function (result) {

        },
        complete: function () {
            $("#btnSaveIntent").prop("disabled", false);
            document.body.style.cursor = prevCursor;
        }
    });
}

function createNewIntentResponseRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="responseOnMouseIn (this)" onmouseout="responseOnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-9" id="obj-intentresponse">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-comments-o text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="intent-response" name="intent-response" style="padding-left: 35px;background-color: #404446; " placeholder="' + value + '">');
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

    checkListIntentResponseIsEmpty();
}

function checkIntentResponseCode(element, key) {
    var value = $(element).val();
    document.getElementById('intent-response').style.borderColor = "#d2d6de";

    if (value.length > 0) {
        document.getElementById('btnAddIntentResponse').disabled = false;
        if (key == 13) {
            if (checkLimitIntentResponse()) {
                document.getElementById('btnAddIntentResponse').disabled = true;
                var parent = document.getElementById('intentresponse-list');
                document.getElementById('intent-response').value = '';
                createNewIntentResponseRow(value, parent);
            }
        }
    }
    else {
        document.getElementById('btnAddIntentResponse').disabled = true;
    }
}

function addIntentResponse() {
    if (checkLimitIntentResponse()) {
        var element = document.getElementById('intent-response');
        var value = $(element).val();
        var parent = document.getElementById('intentresponse-list');
        document.getElementById('intent-response').value = '';
        createNewIntentResponseRow(value, parent);
    }
}

function deleteIntentResponse(element) {
    // delete node from page - dipendence parentNode
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListIntentResponseIsEmpty();
    $("#btnSaveIntent").prop("disabled", false);
}

function checkListIntentResponseIsEmpty() {
    if (document.getElementById('intentresponse-list').childElementCount > 0) {
        $("#btnSaveIntent").prop("disabled", false);
        $("#btnAddIntentResponse").prop("disabled", false);
    }
    else {
        $("#btnSaveIntent").prop("disabled", true);
        $("#btnAddIntentResponse").prop("disabled", true);
    }
}

function checkLimitIntentResponse() {
    var limitTextInputSize = 50;
    switch (limitText($("#intent-response"), limitTextInputSize)) {
        case -1:
            return false;
        case 0:
            msgAlertIntentResponse(0, 'You can add response');
            return true;
        case 1:
            msgAlertIntentResponse(1, 'Limit \'response \' reached!');
            return false;
    }
}

function responseOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function responseOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}
