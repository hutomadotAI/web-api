document.getElementById("addParameter").addEventListener("click", addEmptyVariableRow);

function loadVariablesFromIntent() {
    if (typeof intent['variables'] == "undefined" || !(intent['variables'] instanceof Array))
        return;

    var list_variables = intent['variables'];

    for (var x in list_variables) {
        var entity = '@' + list_variables[x].entity_name;
        var n_prompts = list_variables[x].n_prompts;
        var value = list_variables[x].value;
        var required = list_variables[x].required;
        var prompts = loadPrompts(list_variables[x].prompts);
        var parent = document.getElementById('parameter-list');
        var len = list_variables[x].prompts.length;
        createNewParameterRow(entity, n_prompts, prompts, len, value, required, parent);
    }
}

function loadPrompts(elements) {
    var values = [];
    for (var i = 0; i < elements.length; i++)
        values.push(addEscapeCharacter(elements[i]));
    return values;
}

function createNewParameterRow(entity, n_prompts, prompts, size, value, required, parent) {

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
        wHTML += drawObj('');
    else
        wHTML += drawObj(entity);

    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control flat no-shadow text-center" id="action-nprompt" name="action-nprompt" style="background-color: transparent; margin:0;" placeholder="' + n_prompts + '" onkeydown="resetBorderHighlightError(this)">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-4">');
    wHTML += ('<div class="text-center" >');

    if (size > 0)
        wHTML += ('<input type="text" class="form-control flat no-shadow text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder=" ... " ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-prompts="' + prompts + '"' + 'onMouseOver="this.style.cursor=\'pointer\'" onclick="resetBorderHighlightError(this);" readonly>');
    else
        wHTML += ('<input type="text" class="form-control flat no-shadow text-center" id="action-prompts" name="action-prompts" style="background-color: transparent; margin:0;"' +
        'placeholder="click to enter" ' +
        'data-toggle="modal" ' +
        'data-target="#boxPrompts" ' +
        'data-prompts=""' + 'onMouseOver="this.style.cursor=\'pointer\'" onclick="resetBorderHighlightError(this);" readonly>');

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
    newNode.style.marginTop = '1px';
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);
}

function drawObj(value) {
    var wHTML = '';
    wHTML += ('<a class="btn btn-select btn-primary btn-select-light" onClick="pushEntitiesList(this)">');
    wHTML += ('<input type="hidden" class="btn-select-input" id="" name="" value="" />');
    if (value != '')
        wHTML += ('<span class="btn-select-value">' + value + '</span>');
    else
        wHTML += ('<span class="btn-select-value">add entity</span>');
    wHTML += ('<span class="btn-select-arrow text-sm glyphicon glyphicon-chevron-down"></span>');
    wHTML += ('<ul style="display: none;">');
    wHTML += ('<li class="selected">' + value + '</li>');
    wHTML += ('</ul>');
    wHTML += ('</a>');
    return wHTML;
}

function isUsedEntities(entity_name) {
    var parent = document.getElementById('parameter-list');
    var len = parent.childElementCount;
    while (len--) {
        var node = parent.children[len].children[0].children[0].children[0];
        var container = $(node).find("ul");
        var elem = container.find("li.selected");
        var text = elem.text();
        if (text.replace(/[@]/g, "") == entity_name)
            return true;
    }
    return false;
}

function pushEntitiesList(node) {
    resetBorderHighlightError(node);

    var container = $(node).find("ul");
    var selected = container.find("li.selected");

    var parent = node.parentElement;
    var ul = parent.children[0].children[3];

    // if dropdown is visible exit without refresh list
    if (ul.style.display == 'block')
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

    for (var x in entityListFromServer) {
        // if a Entity is just used , it mush remove from possible selection on dropdown menu but add if is itself
        if (!isUsedEntities(entityListFromServer[x]) || selected.text().replace(/[@]/g, "") == entityListFromServer[x]) {
            var elem = document.createElement('li');
            // if elem was selected, maintain this selection on new list
            if (selected.text().replace(/[@]/g, "") == entityListFromServer[x])
                elem.className = 'selected';
            elem.innerHTML = '@' + entityListFromServer[x];
            container.append(elem);
        }
    }
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
    createNewParameterRow('', 3, '', 0, '', 1, node);
}

function deleteIntentVariable(element) {
    // delete node from page - dipendence parentNode
    var parent = (((element.parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent);
    resetMsgAlertIntentVariable();
}

function resetBorderHighlightError(node) {
    node.style.border = "";
    resetMsgAlertIntentVariable();
}

function resetMsgAlertIntentVariable() {
    msgAlertIntentVariable(ALERT.BASIC.value, 'Use intents to map what a user says and what action should be taken by your business logic.');
    msgAlertIntentVariable(ALERT.BASIC.value, 'Describe what variables you want the AI to extract from a conversation');
}

function isJustAddedNewRow() {
    var parent = document.getElementById('parameter-list');
    // if parameter list is empty then you can ADD new empty row
    if (!parent.hasChildNodes())
        return false;

    // if entity field value is default value it means you just add a new row
    var node = parent.children[0].children[0].children[0].children[0];
    var elem = $(node).find("ul").find("li.selected");
    if (elem.text() == '') {
        msgAlertIntentVariable(ALERT.WARNING.value, 'Complete field first before add a new line');
        return true;
    }

    var len = parent.childElementCount;
    if (len == entityListFromServer.length) { // if are reach the max number of entity
        msgAlertIntentVariable(ALERT.WARNING.value, 'All entities are used!');
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