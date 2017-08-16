document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);

function checkEntityCode(element, key) {
    if (key === 13) {
        if (activeButtonCreateEntity()) {
            postingEntityName();
        }
    } else {
        activeButtonCreateEntity();
    }
}

function activeButtonCreateEntity() {
    var limitTextInputSize = 250;
    switch (limitText($("#inputEntityName"), limitTextInputSize)) {
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
            return false;
    }
}

function postingEntityName() {
    $(this).prop("disabled", true);
    document.getElementById("btnCreateEntity").removeEventListener("click", postingEntityName);

    if (isInputInvalid($("#inputEntityName").val(), 'entity_name')) {
        msgAlertEntity(ALERT.DANGER.value, 'Entity name can contain only the following: A-Z, a-z, 0-9 and _');
        document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);
        return false;
    }

    if (isNameExists($("#inputEntityName").val(), entities)) {
        msgAlertEntity(ALERT.DANGER.value, 'Entity name already exists. Please choose a different name.');
        document.getElementById("btnCreateEntity").addEventListener("click", postingEntityName);
        return false;
    }

    submitElementClicked(inputEntityName.value);
    RecursiveUnbind($('#wrapper'));
    return true;
}

function showEntities(filter) {
    if (entities.length < 1) {
        msgAlertEntity(ALERT.BASIC.value, 'No entities yet. Create the first one.');
        return;
    } else {
        msgAlertEntity(ALERT.BASIC.value, 'In this section you can create different entities.');
    }

    var view = {
        entities: entities.filter(function (item) {
            return !(filter.trim() !== "" && item.entity_name.toLowerCase().indexOf(filter.toLowerCase()) === -1);
        }).sort(function (a, b) {
            if (a.is_system > b.is_system)
                return 1;
            else if (a.is_system > b.is_system)
                return -1;
            if (a.entity_name > b.entity_name)
                return 1;
            else if (a.entity_name < b.entity_name)
                return -1;
            return 0;
        }).map(function (entity, index) {
            entity.index = index;
            return entity;
        })
    };

    $.get('templates/entities_list.mst', function (template) {
        $('#entsearch').html(Mustache.render(template, view));
    });
}

function deleteEntity(elem) {
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';

    msgAlertEntity(ALERT.WARNING.value, 'Deleting...');

    $.ajax({
        url: './proxy/entityProxy.php?entity=' + entities[elem]['entity_name'],
        type: 'DELETE',
        success: function (response) {
            var JSONdata = JSON.parse(response);
            switch (JSONdata['status']['code']) {
                case 200:
                    entities.splice(elem, 1);
                    showEntities('');
                    break;
                default:
                    msgAlertEntity(ALERT.DANGER.value, JSONdata['status']['info']);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            msgAlertEntity(ALERT.DANGER.value, 'Something unexpected occurred - entity not deleted.');
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        }
    });
}

function OnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function editEntity(elem, entity) {
    elem.setAttribute('onclick', '');
    submitElementClicked(entity.replace(/@/g, ""));
}

function submitElementClicked(value) {
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