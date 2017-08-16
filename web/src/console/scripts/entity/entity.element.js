document.getElementById("btnAddEntityValue").addEventListener("click", addEntityValue);

function checkValueCode(element, key) {
    if (key === 13) {
        if( activeButtonCreateEntityValue())
            addEntityValue();
    }
    else {
        activeButtonCreateEntityValue();
    }
}

function activeButtonCreateEntityValue() {
    var limitTextInputSize = 250;
    switch (limitText($("#value-entity"), limitTextInputSize)) {
        case 0:
            msgAlertEntityValues(ALERT.BASIC.value, 'You can add additional values for the current entity.');
            return true;
        case 1:
            msgAlertEntityValues(ALERT.WARNING.value, 'The value name\'s is too long!');
            return false;
        default:
    }
    return false;
}

function addEntityValue() {
      if (isInputInvalid($("#value-entity").val(), 'entity_value')) {
        msgAlertEntityValues(ALERT.DANGER.value, 'Value name can contain only the following: A-Z, a-z, 0-9, _character');
        return;
    }

    var values = [];
    var elements = document.getElementsByName('value-entity-row');
    for (var i = 0; i < elements.length; i++) {
        values.push(elements[i].value);
    }

    if(isNameExists($("#value-entity").val(),values)){
        msgAlertEntityValues(ALERT.DANGER.value, 'Value name already exists. Please choose a different name.');
        return;
    }

    var element = document.getElementById('value-entity');
    var value = element.value;
    var parent = document.getElementById('entityValues-list');
    document.getElementById('value-entity').value = '';
    createNewValueEntityRow(value, parent);
    msgAlertEntityValues(ALERT.BASIC.value,'You can add additional values for the current entity.');
}

function createNewValueEntityRow(value, parent) {
    var view = {
        value: value
    };
    $.get('templates/entity_value_row.mst', function(template) {
        var newNode = document.createElement('div');
        newNode.setAttribute('class', 'col-xs-12');
        newNode.innerHTML = Mustache.render(template, view);
        parent.insertBefore(newNode, parent.firstChild);
    });
}

function deleteValueEntity(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent)
}

function OnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

$(document).ready(function () {
    // loading stored entities values
    for (var x in entityValuesListFromServer) {
        var value = entityValuesListFromServer[x];
        var parent = document.getElementById('entityValues-list');
        document.getElementById('value-entity').value = '';
        createNewValueEntityRow(value, parent);
    }
});

function saveEntity() {
    var values = [];
    var entityName = document.getElementById('entity-name').value;
    var elements = document.getElementsByName('value-entity-row');

    if (elements.length === 0){
        msgAlertEntityValues(ALERT.WARNING.value, 'Please enter at least one value for this entity.');
        return;
    }
    for (var i = 0; i < elements.length; i++) {
         values.push(elements[i].value);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveEntity").prop("disabled", true);

    msgAlertEntityValues(ALERT.WARNING.value, 'Saving...');

    commonAjaxApiRequest({
        url: './proxy/entityProxy.php?entity=' + entityName,
        data: {
            values: values
        },
        verb: 'PUT',
        onOK: function() {
            msgAlertEntityValues(ALERT.PRIMARY.value, 'Entity saved.');
        },
        onGenericError: function() {
            msgAlertEntityValues(ALERT.DANGER.value, 'Entity not saved.');
        },
        onShowError: function(message) {
            msgAlertEntityValues(ALERT.DANGER.value, message);
        },
        onComplete: function() {
            $("#btnSaveEntity").prop("disabled", false);
            document.body.style.cursor = prevCursor;
        }
    });
}