document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);

if (limitText($("#inputEntityName")) == 0)
    $("#btnCreateEntity").prop("disabled", false);

function checkEntityCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateEntity())
            postingEntityName();
    }
    else {
        activeButtonCreateEntity();
    }
}

function activeButtonCreateEntity() {
    var limitTextInputSize = 250;
    switch (limitText($("#inputEntityName"), limitTextInputSize)) {
        case -1:
            $("#btnCreateEntity").prop("disabled", true);
            return false;
        case 0:
            msgAlertEntity(ALERT.BASIC.value, 'In this section you can create different entities.');
            $("#btnCreateEntity").prop("disabled", false);
            return true;
        case 1:
            msgAlertEntity(ALERT.WARNING.value, 'Entity name\'s too long!');
            $("#btnCreateEntity").prop("disabled", false);
            return false;
        default:
            $("#btnCreateEntity").prop("disabled", true);
    }
    return false;
}

function postingEntityName() {
    $(this).prop("disabled", true);
    document.getElementById("btnCreateEntity").removeEventListener("click", postingEntityName);

    if (isInputInvalid($("#inputEntityName").val(), 'entity_name')) {
        msgAlertEntity(ALERT.DANGER.value, 'Entity name can contain only the following: A-Z, a-z, 0-9 and _');
        document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);
        return false;
    }
    
    if(isNameExists($("#inputEntityName").val(),entities)){
        msgAlertEntity(ALERT.DANGER.value, 'Entity name already exists. Please choose a different name.');
        document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);
        return false;
    }

    submitElementClicked(inputEntityName.value);
    RecursiveUnbind($('#wrapper'));
}

function showEntities(str) {
    var wHTML = "";

    if (entities.length < 1) {
        msgAlertEntity(ALERT.BASIC.value, 'No entities yet. Create the first one.');
        return;
    }
    else
        msgAlertEntity(ALERT.BASIC.value, 'In this section you can create different entities.');

    for (var x in entities) {
        if ((str != " ") && ( (str.length == 0) || (entities[x].toLowerCase()).indexOf(str.toLowerCase()) != -1 )) {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body flat no-padding" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row item-row">');

            wHTML += ('<div class="col-xs-10 no-padding" id="obj-entity">');
            wHTML += ('<input type="text" class="form-control flat no-shadow" id="entity-label' + x + '"  name="entity-label" onClick="editEntity(this,this.value)" onMouseOver="this.style.cursor=\'pointer\'" style="padding-left:10px; background-color: #404446; " value="@' + entities[x] + '" readonly>');
            wHTML += ('</div>');

            wHTML += ('<div class="col-xs-2" id="btnEnt"  style="display:none;margin-top:8px;padding-righ:8px;" >');
            wHTML += ('<div class="btn-group pull-right text-gray">');

            wHTML += ('<a data-toggle="modal" data-target="#deleteEntity" id="' + x + '" style="cursor: pointer;">');
            wHTML += ('<i class="fa fa-trash-o text-gray" data-toggle="tooltip" title="Delete"></i>');
            wHTML += ('</a>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</div>');
        }

    }

    newNode.innerHTML = wHTML;
    document.getElementById('entsearch').appendChild(newNode);
}

function deleteEntity(elem) {
    this.location.href = 'entity.php?deleteentity=' + entities[elem];
}

function OnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function submitElementClicked(value){
    var form = document.createElement('form');
    var element = document.createElement('input');

    form.method = 'POST';
    form.action = './entityelement.php';

    element.value = value;
    element.name = 'entity';
    element.setAttribute("type", "hidden");
    form.appendChild(element);
    document.body.appendChild(form);
    form.submit();
}

function editEntity(elem,entity) {
    elem.setAttribute('onclick','');
    submitElementClicked(entity.replace(/@/g, ""));
}

$('#deleteEntity').on('show.bs.modal', function (e) {
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-entity-label');
    var elemBtn = document.getElementById('modalDelete');
    var value = $('#entity-label' + esseyId).text();
    elem.innerHTML = 'Are you sure you want to delete the entity <label>' + value + '</label>?';
    elemBtn.setAttribute("value", esseyId);
});

// VIDEO TUTORIAL
$("#collapseVideoTutorialEntity").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});