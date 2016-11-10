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
    alert(curr_entity);
    alert(curr_intent);
    $(e.currentTarget).find('input[name="curr_entity"]').val(curr_entity);
    $(e.currentTarget).find('input[name="curr_intent"]').val(curr_intent);
    $(e.currentTarget).find('input[name="curr_n_prompts"]').val(curr_n_prompts);

    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    cleanupromptDialogbox();
    loadPromptsForEntity(curr_entity)
});

$(document).ready(function () {
    loadVariablesFromIntent();
    loadEntities();
});


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
