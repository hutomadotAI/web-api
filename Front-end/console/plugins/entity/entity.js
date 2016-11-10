document.getElementById("inputEntityName").addEventListener("keyup", activeButtonCreateEntity);
document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);

if (limitText($("#inputEntityName")) == 0)
    $("#btnCreateEntity").prop("disabled", false);

function activeButtonCreateEntity() {
    var limitTextInputSize = 50;
    switch (limitText($("#inputEntityName"), limitTextInputSize)) {
        case -1:
            $("#btnCreateEntity").prop("disabled", true);
            break;
        case 0:
            msgAlertEntity(0, 'In this section you can create different entities.');
            $("#btnCreateEntity").prop("disabled", false);
            break;
        case 1:
            msgAlertEntity(1, 'The entity name is too long!');
            $("#btnCreateEntity").prop("disabled", false);
            break;
        default:
            $("#btnCreateEntity").prop("disabled", true);
    }
}

function postingEntityName() {
    $(this).prop("disabled", true);

    if (inputValidation($("#inputEntityName").val(), 'entity_name')) {
        msgAlertEntity(2, 'Entity name need contain only the following: A-Z, a-z, 0-9 character');
        return;
    }
    
    if(isNameExists($("#inputEntityName").val(),entities)){
        msgAlertEntity(2, 'Two identical Entity names are not allowed. Please choose a different name.');
        return;
    }

    if (document.entityCreateForm.onsubmit)
        return;

    RecursiveUnbind($('#wrapper'));
    document.entityCreateForm.submit();
}

function showEntities(str) {
    var wHTML = "";

    if (entities.length < 1) {
        msgAlertEntity(0, 'No entities yet. Create the first one.');
        return;
    }
    else
        msgAlertEntity(0, 'In this section you can create different entities.');

    for (var x in entities) {
        if ((str != " ") && ( (str.length == 0) || (entities[x].toLowerCase()).indexOf(str.toLowerCase()) != -1 )) {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body flat" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');

            wHTML += ('<div class="col-xs-9" id="obj-entity">');
            wHTML += ('<div class="text-gray" type="submit" id="entity-label' + x + '" onClick="editEntity(this.innerHTML)" onMouseOver="this.style.cursor=\'pointer\'">@' + entities[x] + '</div>')
            wHTML += ('</div>');

            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray">');

            var unique_id = 'collapsePromptInfo_' + new Date().getTime().toString() + (entities[x].replace(/\s/g, '') + x);
            wHTML += ('<a data-toggle="collapse" href="#' + unique_id + '">');
            wHTML += ('<i class="fa fa-comments-o text-gray" data-toggle="tooltip" title="prompt response" style="padding-right:7px;"></i>');
            wHTML += ('</a>');

            wHTML += ('<a data-toggle="dropdown">');
            wHTML += ('<i class="fa fa-cloud-download text-gray" style="padding-right: 5px;" data-toggle="tooltip" title="Download"></i>');
            wHTML += ('</a>');
            wHTML += ('<ul class="dropdown-menu flat">');
            wHTML += ('<li><a onClick="downloadEntity (\'' + entities[x] + '\',' + x + ',0)">JSON format</a></li>');
            wHTML += ('<li><a onClick="downloadEntity (\'' + entities[x] + '\',' + x + ',1)">CSV table</a></li>');
            wHTML += ('</ul>');
            wHTML += ('<a data-toggle="modal" data-target="#deleteEntity" id="' + x + '" style="cursor: pointer;">');
            wHTML += ('<i class="fa fa-trash-o text-gray" data-toggle="tooltip" title="Delete"></i>');
            wHTML += ('</a>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            // push VALUES inside box internal
            wHTML += ('<div id="' + unique_id + '" class="panel-collapse collapse" >');
            wHTML += ('<div class="row" style="padding: 10px 0px 0px 0px;">');
            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="inner-addon left-addon">');
            wHTML += ('<i class="fa fa-comments-o text-gray"></i>');
            wHTML += ('<textarea class="form-control flat no-shadow" id="prompt-key" name="prompt-key" rows="2" style="padding-left: 35px; background-color: #515151; border: 1px solid #202020;" placeholder="value from server" readonly></textarea>');
            wHTML += ('</div>');
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

function editEntity(entity) {
    var form = document.createElement('form');
    var element = document.createElement('input');

    form.method = 'POST';
    form.action = './entityelement.php';

    element.value = entity.replace(/@/g, "");
    element.name = 'entity';
    form.appendChild(element);
    document.body.appendChild(form);
    form.submit();
}

function downloadEntity(name, value, flag) {
    name = name.replace(/[\|&;\$%@"<>\(\)\+,]/g, "");
    if (flag === 0) {
        var blob = new Blob(["this file contains entity in JSON format"], {type: "text/plain;charset=utf-8;",});
        saveAs(blob, name + ".txt");
    }
    else {
        var blob = new Blob(["this file is a simulaion of CVS format file"], {type: "text/plain;charset=utf-8;",});
        saveAs(blob, name + ".csv");
    }
}

$('#deleteEntity').on('show.bs.modal', function (e) {
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-entity-label');
    var elemBtn = document.getElementById('modalDelete');
    var value = $('#entity-label' + esseyId).text();
    elem.innerHTML = 'Are you sure you would like to delete the <label>' + value + '</label> entity ? ';
    elemBtn.setAttribute("value", esseyId);
});

// VIDEO TUTORIAL
$("#collapseVideoTutorialEntity").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});