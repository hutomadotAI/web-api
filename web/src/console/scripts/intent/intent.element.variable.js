document.getElementById("addParameter").addEventListener("click", addEmptyVariableRow);

function loadVariablesFromIntent() {
    if (typeof intent['variables'] === "undefined" || !(intent['variables'] instanceof Array))
        return;

    for (var x in intent['variables']) {
        var v = intent['variables'][x];
        var entity = '@' + v.entity_name;
        var n_prompts = v.n_prompts;
        var value = v.value;
        var required = v.required;
        var prompts = loadPrompts(v.prompts);
        var parent = document.getElementById('parameter-list');
        var label = (typeof v.label !== 'undefined') ? v.label : '';
        createNewParameterRow(entity, n_prompts, prompts, prompts.length, value, required, label, parent);
    }
}

function loadPrompts(elements) {
    var values = [];
    for (var i = 0; i < elements.length; i++)
        values.push(addEscapeCharacter(elements[i]));
    return values;
}

function createNewParameterRow(entity, n_prompts, prompts, size, value, required, label, parent) {

    if (isJustAddedNewRow())
        return;

    if (typeof(n_prompts) === 'undefined' || (n_prompts) === '')
        n_prompts = 'n° prompt';
    if (typeof(value) === 'undefined' || (value) === '')
        value = 'insert value';

    var wHTML = '';


    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');

    if (typeof(entity) === 'undefined' || (entity) === '')
        wHTML += drawObj('');
    else
        wHTML += drawObj(entity);

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control flat no-shadow text-center" name="action-nprompt" style="background-color: transparent; margin:0;" placeholder="'
        + n_prompts + '" onclick="resetBorderHighlightError(this)" onkeydown="enableSaving(true)">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2">');
    wHTML += ('<div class="text-center" >');

    if (size > 0)
        wHTML += ('<input type="text" class="form-control flat no-shadow text-center" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder=" ... " ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-prompts="' + prompts + '"' + ' onMouseOver="this.style.cursor=\'pointer\'" onclick="resetBorderHighlightError(this);" readonly>');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow text-center" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder="click to enter" ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-prompts=""' + ' onMouseOver="this.style.cursor=\'pointer\'" onclick="resetBorderHighlightError(this);" readonly>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" style="padding-top:7px;">');
    wHTML += ('<div class="text-left" >');
    wHTML += ('<div class="col-xs-7 text-gray no-padding">');

    if (required === 0)
        wHTML += ('<input class="pull-right" type="checkbox" name="action-required" onclick="enableSaving(true)"> ');
    else
        wHTML += ('<input class="pull-right" type="checkbox" name="action-required" onclick="enableSaving(true)" checked> ');

    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" style="padding-top:7px;">');
    wHTML += ('<div class="text-left" >');
    wHTML += ('<div class="text-gray no-padding">');
    wHTML += ('<input type="text" class="form-control flat no-shadow text-center" name="action-nprompt" '
        + 'style="background-color: transparent; margin:0;" value="' + label
        + '" onclick="resetBorderHighlightError(this)" onkeydown="enableSaving(true)">');
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');


    wHTML += ('<div class="col-xs-1 text-gray no-padding">');
    wHTML += ('<a class="pull-right"  data-toggle="modal" data-target="#deleteIntentVariable" style="display:none; padding-top:2px;" onClick="deleteIntentVariable(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete" style="padding-top:10px;padding-right:10px;"></i>');
    wHTML += ('</a>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'box-body flat no-padding');
    newNode.setAttribute('onmouseover', 'variableOnMouseIn (this)');
    newNode.setAttribute('onmouseout', 'variableOnMouseOut (this)');

    newNode.style.backgroundColor = '#404446';
    newNode.style.marginTop = '1px';
    newNode.innerHTML = wHTML;
    parent.appendChild(newNode);
}

function drawObj(value) {
    var wHTML = '';
    wHTML += ('<a class="btn btn-select btn-primary btn-select-light" onClick="pushEntitiesList(this)">');
    wHTML += ('<input type="hidden" class="btn-select-input" value="" />');
    if (value !== '')
        wHTML += ('<span class="btn-select-value">' + value + '</span>');
    else
        wHTML += ('<span class="btn-select-value">add entity</span>');
    wHTML += ('<span class="btn-select-arrow text-sm glyphicon glyphicon-chevron-down"></span>');
    wHTML += ('<ul class="flex">');
    wHTML += ('<li class="selected">' + value + '</li>');
    wHTML += ('</ul>');
    wHTML += ('</a>');
    return wHTML;
}

function pushEntitiesList(node) {
    resetBorderHighlightError(node);

    var container = $(node).find("ul");
    var selected = container.find("li.selected");

    var parent = node.parentElement;
    var ul = parent.children[0].children[3];

    // if dropdown is visible exit without refresh list
    if (ul.style.display === 'block')
        return;

    // remove all list of child inside UL node
    var fc = ul.firstChild;
    while (fc) {
        ul.removeChild(fc);
        fc = ul.firstChild;
    }

    if (entityListFromServer.length < 1) {
        msgAlertIntentVariable(ALERT.WARNING.value, 'No entities available.');
        return;
    }

    var listEntities = [];
    // non-system entities at the top
    entityListFromServer.map(function(entity) { if (!entity['is_system']) { listEntities.push(entity); }} );
    entityListFromServer.map(function(entity) { if (entity['is_system']) { listEntities.push(entity); }} );
    listEntities.map(function(entity) {
        var name = '@' + entity['entity_name'];
        var elem = document.createElement('li');
        elem.setAttribute('data-toggle', 'tooltip');
        elem.setAttribute('title', entity['entity_name']);
        // if elem was selected, maintain this selection on new list
        if (selected.text() === name)
            elem.className = 'selected';
        else
            elem.setAttribute('onClick','enableSaving(true)');
        elem.innerHTML = name;
        container.append(elem);
    });
}

function variableOnMouseIn(elem) {
    var btn = elem.children[5].children[0];
    btn.style.display = '';
}

function variableOnMouseOut(elem) {
    var btn = elem.children[5].children[0];
    btn.style.display = 'none';
}

function addEmptyVariableRow() {
    var node = document.getElementById('parameter-list');
    createNewParameterRow('', 3, '', 0, '', 1, '', node);
}

function deleteIntentVariable(element) {
    // delete node from page - dipendence parentNode
    var parent = element.parentNode.parentNode;
    parent.parentNode.removeChild(parent);
    resetMsgAlertIntentVariable();
    enableSaving(true);
}

function resetBorderHighlightError(node) {
    node.style.border = "";
    resetMsgAlertIntentVariable();
}

function resetMsgAlertIntentVariable() {
    msgAlertIntentVariable(ALERT.BASIC.value, 'Use intents to map what the user says and what actions should be taken by your business logic.');
}

function isJustAddedNewRow() {
    var parent = document.getElementById('parameter-list');
    // if parameter list is empty then you can ADD new empty row
    if (!parent.hasChildNodes())
        return false;

    // if entity field value is default value it means you just add a new row
    var node = parent.children[0].children[0].children[0].children[0];
    var elem = $(node).find("ul").find("li.selected");
    if (elem.text() === '') {
        msgAlertIntentVariable(ALERT.WARNING.value, 'Complete all fields first before adding a new line.');
        return true;
    }

    var len = parent.childElementCount;
    if (len === entityListFromServer.length) { // if are reach the max number of entity
        msgAlertIntentVariable(ALERT.WARNING.value, 'All available entities are already added to this intent.');
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