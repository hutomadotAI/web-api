document.getElementById("addParameter").addEventListener("click", addEmptyVariableRow);

function loadExpressionsFromIntent() {
    if (typeof intent['user_says'] == "undefined" || !(intent['user_says'] instanceof Array))
        return;

    var list_expressions = intent['user_says'];
    for (var i in list_expressions) {

        var value = list_expressions[i];
        var parent = document.getElementById('userexpression-list');
        document.getElementById('user-expression').value = '';
        createNewUsersayRow(value, parent);
    }
}

function loadVariablesFromIntent() {
    if (typeof intent['variables'] == "undefined" || !(intent['variables'] instanceof Array))
        return;

    var list_variables = intent['variables'];
    var intent_name = intent['intent_name'];

    for (var x in list_variables) {

        var entity = '@' + list_variables[x].entity_name;
        var n_prompts = list_variables[x].n_prompts;
        var value = list_variables[x].value;
        var required = list_variables[x].required;
        var prompts = list_variables[x].prompts;
        var parent = document.getElementById('parameter-list');
        var len = list_variables[x].prompts.length;
        createNewParameterRow(entity, intent_name, n_prompts, prompts, len, value, required, parent);
    }
}

function loadResponsesFromIntent() {
    if (typeof intent['responses'] == "undefined" || !(intent['responses'] instanceof Array))
        return;

    var list_responses = intent['responses'];
    for (var i in list_responses) {

        var value = list_responses[i];
        var parent = document.getElementById('intentresponse-list');
        document.getElementById('intent-response').value = '';
        createNewIntentResponseRow(value, parent);
    }
}


function loadEntities() {
    var array = [];
    for (var x in entityListFromServer) {
        array.push('@' + entityListFromServer[x]);
    }
    var $input = $('#action-entity');

    $input.omniselect({
        source: array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function (label, id, index) {
            return '<li><a href="#">' + label + '</a></li>';
        }
    });

    $input.on('omniselect:select', function (event, value) {
        console.log('Selected: ' + value);
    });

}

function addEmptyVariableRow() {
    var node = document.getElementById('parameter-list');
    createNewParameterRow('', '', '', '', 0, '', false, node);
}

function deleteIntentVariable(element) {
    // delete node from page - dipendence parentNode
    var parent = (((element.parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent);
    checkListExpressionIsEmpty();
    resetMsgAlertIntentVariable();
    releaseUsedEntities();
}

function resetMsgAlertIntentVariable() {
    containerMsgAlertIntentVariable(0, 'Set the parameters for the intents using existing entities.');
}

function variableOnMouseIn(elem) {
    var btn = elem.children[3].children[0].children[1].children[0];
    btn.style.display = '';
}

function variableOnMouseOut(elem) {
    var btn = elem.children[3].children[0].children[1].children[0];
    btn.style.display = 'none';
}

function createNewParameterRow(entity, intent_name, n_prompts, prompts, size, value, required, parent) {

    if (isJustAddedNewRow())
        return;

    if (typeof(n_prompts) === 'undefined' || (n_prompts) == '')
        n_prompts = 'n° prompt';
    if (typeof(value) === 'undefined' || (value) == '')
        value = 'insert value';

    var wHTML = '';

    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');

    if (typeof(entity) === 'undefined' || (entity) == '')
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border" name="action-entity"  id="action-entity" style="background-color: transparent; margin:0;" placeholder="add entity" autocomplete="off" >');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border" name="action-entity"  id="action-entity" style="background-color: transparent; margin:0;" placeholder="' + entity + '" autocomplete="off" disabled="disabled">');

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-nprompt" name="action-nprompt" style="background-color: transparent; margin:0;" placeholder="' + n_prompts + '" >');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-4">');
    wHTML += ('<div class="text-center" >');


    if (size > 0)
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder="' + prompts + '" data-toggle="modal" data-target="#boxPrompts" ' +
        'data-entity="' + entity + '" ' +
        'data-intent="' + intent_name + '"' +
        'data-nprompts="' + n_prompts + '" onMouseOver="this.style.cursor=\'pointer\'">');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder="click to enter" data-toggle="modal" data-target="#boxPrompts" ' +
        'data-entity="' + entity + '" ' +
        'data-intent="' + intent_name + '"' +
        'data-nprompts="' + n_prompts + '" onMouseOver="this.style.cursor=\'pointer\'">');

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" style="padding-top:7px;">');
    wHTML += ('<div class="text-left" >');
    wHTML += ('<div class="col-xs-7 text-gray no-padding">');

    if (required == 0)
        wHTML += ('<input class="pull-right" type="checkbox" id="required" name="action-required"> ');
    else
        wHTML += ('<input class="pull-right" type="checkbox" id="required" name="action-required" checked> ');

    wHTML += ('</div>');
    wHTML += ('<div class="col-xs-5 text-gray no-padding">');
    wHTML += ('<a class="pull-right"  data-toggle="modal" data-target="#deleteIntentVariable" style="display:none; padding-top:2px;" onClick="deleteIntentVariable(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    wHTML += ('</div>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'box-body flat no-padding');
    newNode.setAttribute('onmouseover', 'variableOnMouseIn (this)');
    newNode.setAttribute('onmouseout', 'variableOnMouseOut (this)');

    newNode.style.backgroundColor = '#404446;';
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    // be carefull - this is the treepath for input entity list
    var inputNode = newNode.children[0].children[0].children[0];
    var array = [];

    // loading stored entities
    for (var x in entityListFromServer) {
        if (typeof(entity) === 'undefined' || (entity) == '') {
            // if a Entity is just used , it mush remove from possible selection on dropdown menu
            if (!isUsedEntities(entityListFromServer[x]))
                array.push('@' + entityListFromServer[x]);
        }
    }

    $(inputNode).omniselect({
        source: array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function (label, id, index) {
            return '<li><a href="#">' + label + '</a></li>';
        }
    });

    // after selection remove value on input text and set placeholder
    $(inputNode).on('omniselect:select', function (event, value) {
        $(inputNode).val('');
        $(inputNode).attr('placeholder', value);
    });
}

function isUsedEntities(entity_name) {
    var parent = document.getElementById('parameter-list');
    //  id NODE - relative at entities are in variables list under entity field
    var len = parent.childElementCount;

    while (--len) {
        var node = parent.children[len].children[0].children[0].children[0];
        if (node.placeholder.replace(/[@]/g, "") == entity_name)
            return true;
    }
    return false;
}

function releaseUsedEntities() {

    var parent = document.getElementById('parameter-list');
    if (!parent.hasChildNodes())
        return;
    var node = parent.children[0].children[0].children[0].children[0];
    var array = [];

    if (node.placeholder != 'add entity')
        return;

    // loading stored entities
    for (var x in entityListFromServer) {
        if (!isUsedEntities(entityListFromServer[x]))
            array.push('@' + entityListFromServer[x]);
    }

    $(node).omniselect({
        source: array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function (label, id, index) {
            return '<li><a href="#">' + label + '</a></li>';
        }
    });

}

function isJustAddedNewRow() {
    var parent = document.getElementById('parameter-list');
    // if parameter list is empty then you can ADD new empty row
    if (!parent.hasChildNodes())
        return false;

    // if entity field value is default value it means you just add a new row
    var node = parent.children[0].children[0].children[0].children[0];
    if (node.placeholder == 'add entity')  // se hai raggiunto in numero massimo MSG ALERT
        return true;

    var len = parent.childElementCount;
    if (len == entityListFromServer.length) { // se hai raggiunto in numero massimo MSG ALERT
        containerMsgAlertIntentVariable(1, 'All entities are used!');
        return true;
    }

    return false;
}


function loadPromptsForEntity(curr_entity) {
    if (typeof intent['variables'] == "undefined" || !(intent['variables'] instanceof Array))
        return;

    var list_variables = intent['variables'];
    var len = list_variables.length;

    while (len--) {
        if (list_variables[len].entity_name == curr_entity) {
            for (var i in list_variables[len].prompts) {
                var value = list_variables[len].prompts[i];
                var parent = document.getElementById('prompts-list');
                document.getElementById('input-prompt').value = '';
                createNewPromptRow(value, parent);
            }
        }
    }
}

function cleanupromptDialogbox() {
    var node = document.getElementById('prompts-list');
    while (node.firstChild) node.removeChild(node.firstChild);
}


// Pass values to Modal on show dialog modal
$('#boxPrompts').on('show.bs.modal', function (e) {

    var curr_entity = $(e.relatedTarget).data('entity');
    var curr_intent = $(e.relatedTarget).data('intent');
    var curr_n_prompts = $(e.relatedTarget).data('nprompts');
    $(e.currentTarget).find('input[name="curr_entity"]').val(curr_entity);
    $(e.currentTarget).find('input[name="curr_intent"]').val(curr_intent);
    $(e.currentTarget).find('input[name="curr_n_prompts"]').val(curr_n_prompts);

    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    cleanupromptDialogbox();
    loadPromptsForEntity(curr_entity)
});

$(document).ready(function () {

    loadExpressionsFromIntent();
    loadVariablesFromIntent();
    loadResponsesFromIntent();
    loadEntities();
    checkListExpressionIsEmpty();
});
