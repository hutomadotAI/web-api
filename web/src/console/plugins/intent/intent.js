document.getElementById("btnCreateIntent").addEventListener("click", postingIntentName);

if (limitText($("#inputIntentName")) == 0)
    $("#btnCreateEntity").prop("disabled", false);

function checkIntentCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateIntent())
            postingIntentName();
    }
    else {
        activeButtonCreateIntent();
    }
}

function activeButtonCreateIntent() {
    var limitTextInputSize = 250;
    switch (limitText($("#inputIntentName"), limitTextInputSize)) {
        case -1:
            $("#btnCreateIntent").prop("disabled", true);
            return false;
        case 0:
            msgAlertIntent(ALERT.BASIC.value, 'Create an Intent to trigger your own business logic.');
            $("#btnCreateIntent").prop("disabled", false);
            return true;
        case 1:
            msgAlertIntent(ALERT.WARNING.value, 'Intent\'s name is too long!');
            $("#btnCreateIntent").prop("disabled", false);
            return false;
        default:
            $("#btnCreateIntent").prop("disabled", true);
    }
    return false;
}

function postingIntentName() {
    $(this).prop("disabled", true);

    if (isInputInvalid($("#inputIntentName").val(), 'intent_name')) {
        msgAlertIntent(ALERT.DANGER.value, 'Intent name can contain only the following: A-Z, a-z, 0-9 and _');
        return false;
    }

    if(isNameExists($("#inputIntentName").val(),intents)){
        msgAlertIntent(ALERT.DANGER.value, 'Intent name already exists. Please choose a different name.');
        return false;
    }

    var form = document.createElement('form');
    var element = document.createElement('input');

    form.method = 'POST';
    form.action = './intentelement.php';

    element.value = inputIntentName.value;
    element.name = 'intent';
    form.appendChild(element);
    document.body.appendChild(form);
    form.submit();

    RecursiveUnbind($('#wrapper'));
}

function showIntents(str) {
    var wHTML = "";

    if (intents.length < 1) {
        msgAlertIntent(ALERT.BASIC.value, 'No intents yet. Create the first one.');
        return;
    }
    else
        msgAlertIntent(ALERT.BASIC.value, 'Create an Intent to trigger your own business logic.');


    for (var x in intents) {
        if ((str != " ") && ( (str.length == 0) || (intents[x].toLowerCase()).indexOf(str.toLowerCase()) != -1 )) {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body flat" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');

            wHTML += ('<div class="col-xs-9" id="obj-entity">');
            wHTML += ('<div class="text-gray" type="submit" id="intent-label' + x + '" onClick="editIntent(this,this.innerHTML)" onMouseOver="this.style.cursor=\'pointer\'">' + intents[x] + '</div>')
            wHTML += ('</div>');

            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray">');

            wHTML += ('<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="download options" tabindex="-1" >');
            wHTML += ('<i class="fa fa-cloud-download text-gray" style="padding-right: 5px;" data-toggle="tooltip" title="Download "></i>');
            wHTML += ('</a>');
            wHTML += ('<ul class="dropdown-menu no-border flat">');
            wHTML += ('<li onMouseOver="this.style.cursor=\'pointer\'"><a onClick="downloadIntent (\'' + intents[x] + '\',' + x + ',0)"><span class="text-white">JSON format</span></a></li>');
            wHTML += ('<li onMouseOver="this.style.cursor=\'pointer\'"><a onClick="downloadIntent (\'' + intents[x] + '\',' + x + ',1)"><span class="text-white">CSV table</span></a></li>');
            wHTML += ('</ul>');
            wHTML += ('<a data-toggle="modal" data-target="#deleteIntent" id="' + x + '" style="cursor: pointer;">');
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
    document.getElementById('intentsearch').appendChild(newNode);
}

function deleteIntent(elem) {
    this.location.href = 'intent.php?deleteintent=' + intents[elem];
}

function OnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function editIntent(elem,intent) {
    elem.setAttribute('onclick','');
    var form = document.createElement('form');
    var element = document.createElement('input');

    form.method = 'POST';
    form.action = './intentelement.php';

    element.value = intent;
    element.name = 'intent';
    form.appendChild(element);
    document.body.appendChild(form);
    form.submit();
}

function downloadIntent(name, value, flag) {
    name = name.replace(/[\|&;\$%@"<>\(\)\+,]/g, "");
    if (flag === 0) {
        var blob = new Blob(["This file contains entities in JSON format."], {type: "text/plain;charset=utf-8;",});
        saveAs(blob, name + ".txt");
    }
    else {
        var blob = new Blob(["This file contains entities in CSV format."], {type: "text/plain;charset=utf-8;",});
        saveAs(blob, name + ".csv");
    }
}

$('#deleteIntent').on('show.bs.modal', function (e) {
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-intent-label');
    var elemBtn = document.getElementById('modalDelete');
    var value = $('#intent-label' + esseyId).text();
    elem.innerHTML = 'Are you sure you would like to delete the <label>' + value + '</label> intent ? ';
    elemBtn.setAttribute("value", esseyId);
});

// VIDEO TUTORIAL
$("#collapseVideoTutorialIntent").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});