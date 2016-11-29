document.getElementById("addParameter").addEventListener("click", addEmptyVariableRow);

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
        var prompts = loadPrompts(list_variables[x].prompts);
        var parent = document.getElementById('parameter-list');
        var len = list_variables[x].prompts.length;
        createNewParameterRow(entity, intent_name, n_prompts, prompts, len, value, required, parent);
    }
}

function loadPrompts(elements){
    var values = [];
    for (var i = 0; i < elements.length; i++)
        values.push(addEscapeCharacter(elements[i]));
    return values;
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
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border" name="action-entity"  id="action-entity" style="background-color: transparent; margin:0;" placeholder="add entity" autocomplete="off" onClick="pushEntitiesList(this)">');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border" name="action-entity"  id="action-entity" style="background-color: transparent; margin:0;" placeholder="' + entity + '" value="' + entity + '" autocomplete="off" onClick="pushEntitiesList(this)">');

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-nprompt" name="action-nprompt" style="background-color: transparent; margin:0;" placeholder="' + n_prompts + '" onkeydown="resetMsgAlertIntentVariable()">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-4">');
    wHTML += ('<div class="text-center" >');

    if (size > 0)
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder=" ... " ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-intent="' + intent_name + '"' +
        'data-prompts="' + prompts + '"' + 'onMouseOver="this.style.cursor=\'pointer\'" readonly>');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow no-border text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder="click to enter" ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-intent="' + intent_name + '"' +
        'data-prompts=""' + 'onMouseOver="this.style.cursor=\'pointer\'" readonly>');

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

    newNode.style.backgroundColor = '#404446';
    newNode.style.marginTop ='1px';
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    // be carefull - this is the treepath for input entity list
    var inputNode = newNode.children[0].children[0].children[0];
    var inputNodePrompt = newNode.children[2].children[0].children[0];

    // after selection add value on input text and set placeholder
    $(inputNode).on('omniselect:select', function (event, value) {
        $(inputNode).attr('value', value);
        $(inputNode).attr('placeholder', value);
        //pass to node prompt on data-prompt attribute the value on current selected entity
        $(inputNodePrompt).attr('data-entity', value);
    });
}

function isUsedEntities(entity_name) {
    var parent = document.getElementById('parameter-list');
    var len = parent.childElementCount;
    while (len--) {
        var node = parent.children[len].children[0].children[0].children[0];
        if (node.placeholder.replace(/[@]/g, "") == entity_name)
            return true;
    }
    return false;
}

function pushEntitiesList(node){
    var new_array = [];
    // loading stored entities
    for (var x in entityListFromServer) {
        // if a Entity is just used , it mush remove from possible selection on dropdown menu but add if is itself
        if (!isUsedEntities(entityListFromServer[x]) ||  node.placeholder.replace(/[@]/g, "") == entityListFromServer[x])
            new_array.push('@' + entityListFromServer[x]);
    }

    $(node).omniselect({
        source: new_array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function (label, id, index) {
            return '<li><a href="#">' +label + '</a></li>';
        }
    });
}

function variableOnMouseIn(elem) {
    var btn = elem.children[3].children[0].children[1].children[0];
    btn.style.display = '';
}

function variableOnMouseOut(elem) {
    var btn = elem.children[3].children[0].children[1].children[0];
    btn.style.display = 'none';
}

function addEmptyVariableRow() {
    var node = document.getElementById('parameter-list');
    var intent_name = intent['intent_name'];
    createNewParameterRow('', intent_name, 3, '', 0, '', false, node);
}

function deleteIntentVariable(element) {
    // delete node from page - dipendence parentNode
    var parent = (((element.parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent);
    resetMsgAlertIntentVariable();
}

function resetMsgAlertIntentVariable() {
    msgAlertIntentVariable(0, 'Set the parameters for the intents using existing entities.');
    msgAlertIntentElement(0,'Set the variables used by the intent.');
}

function isJustAddedNewRow() {
    var parent = document.getElementById('parameter-list');
    // if parameter list is empty then you can ADD new empty row
    if (!parent.hasChildNodes())
        return false;

    // if entity field value is default value it means you just add a new row
    var node = parent.children[0].children[0].children[0].children[0];
    if (node.placeholder == 'add entity') {
        msgAlertIntentVariable(1, 'Complete field first before add a new line');
        return true;
    }

    var len = parent.childElementCount;
    if (len == entityListFromServer.length) { // se hai raggiunto in numero massimo MSG ALERT
        msgAlertIntentVariable(1, 'All entities are used!');
        return true;
    }
    return false;
}

function cleanupromptDialogbox() {
    var node = document.getElementById('prompts-list');
    while (node.firstChild) node.removeChild(node.firstChild);
}

$(document).ready(function () {
    loadVariablesFromIntent();
});

