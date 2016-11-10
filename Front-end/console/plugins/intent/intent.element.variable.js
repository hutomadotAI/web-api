document.getElementById("addParameter").addEventListener("click", addEmptyVariableRow);

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
    createNewParameterRow('', '', '', '', 0, '', false, node);
}

function deleteIntentVariable(element) {
    // delete node from page - dipendence parentNode
    var parent = (((element.parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent);
    resetMsgAlertIntentVariable();
    releaseUsedEntities();
}

function resetMsgAlertIntentVariable() {
    containerMsgAlertIntentVariable(0, 'Set the parameters for the intents using existing entities.');
}


