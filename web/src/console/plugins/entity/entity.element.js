document.getElementById("btnAddEntityValue").addEventListener("click", addEntityValue);

if (limitText($("#value-entity")) == 0)
    $("#btnAddEntityValue").prop("disabled", false);

function checkValueCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateEntityValue())
            addEntityValue();
    }
    else {
        activeButtonCreateEntityValue();
    }
}

function activeButtonCreateEntityValue() {
    var limitTextInputSize = 50;
    switch (limitText($("#value-entity"), limitTextInputSize)) {
        case -1:
            $("#btnAddEntityValue").prop("disabled", true);
            return false;
        case 0:
            msgAlertEntityValues(ALERT.BASIC.value, 'You can add additional values for the current entity.');
            $("#btnAddEntityValue").prop("disabled", false);
            return true;
        case 1:
            msgAlertEntityValues(ALERT.WARNING.value, 'The value name\'s is too long!');
            $("#btnAddEntityValue").prop("disabled", true);
            return false
        default:
            $("#btnAddEntityValue").prop("disabled", true);
    }
    return false;
}

function addEntityValue() {
    $(this).prop("disabled", true);

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
    var value = $(element).val();
    var parent = document.getElementById('entityValues-list');
    document.getElementById('value-entity').value = '';
    createNewValueEntityRow(value, parent);
    msgAlertEntityValues(ALERT.BASIC.value,'You can add additional values for the current entity.');
}

function createNewValueEntityRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-9" id="obj-value-entity" >');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-sign-out text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="value-entity-row" name="value-entity-row" style="padding-left: 35px;background-color: #404446; " value="' + value + '" placeholder="' + value + '">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3" id="btnValueEntity" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');

    wHTML += ('<a data-toggle="modal" data-target="#deleteValueEntity" style="padding-right:3px; cursor: pointer;" onClick="deleteValueEntity(this)">');
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

    checkListEntityValuesSize();
}

function checkListEntityValuesSize() {
    if (document.getElementById('entityValues-list').childElementCount > 0)
        $("#btnSaveEntity").prop("disabled", false);
    else
        $("#btnSaveEntity").prop("disabled", true);
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

    if (elements.length == 0){
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

    $.ajax({
        url: './dynamic/updateEntity.php',
        data: {
            entity_name: entityName, entity_values: values
        },
        type: 'POST',
        success: function (response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    msgAlertEntityValues(ALERT.PRIMARY.value, 'Entity saved.');
                    break;
                case 400:
                    msgAlertEntityValues(ALERT.DANGER.value, JSONdata['status']['info']);
                    break;
                case 500:
                    msgAlertEntityValues(ALERT.DANGER.value, JSONdata['status']['info']);
                    break;
                default:
                    msgAlertEntityValues(ALERT.DANGER.value, JSONdata['status']['info']);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            msgAlertEntityValues(ALERT.DANGER.value, 'Entity not saved.');
        },
        complete: function () {
            checkListEntityValuesSize();
            document.body.style.cursor = prevCursor;
        }
    });
}