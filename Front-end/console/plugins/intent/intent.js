document.getElementById("inputIntentName").addEventListener("keydown", activeButtonCreateIntent);
document.getElementById("btnCreateIntent").addEventListener("click", postingIntentName);

if (limitText($("#inputIntentName")) == 0)
    $("#btnCreateEntity").prop("disabled", false);

function activeButtonCreateIntent() {
    var limitTextInputSize = 50;
    switch (limitText($("#inputIntentName"), limitTextInputSize)) {
        case -1:
            $("#btnCreateIntent").prop("disabled", true);
            break;
        case 0:
            msgAlertIntent(0, 'Create an Intent to trigger your own business logic.');
            $("#btnCreateIntent").prop("disabled", false);
            break;
        case 1:
            msgAlertIntent(1, 'The intent name is too long!');
            $("#btnCreateIntent").prop("disabled", false);
            break;
        default:
            $("#btnCreateIntent").prop("disabled", true);
    }
}

function postingIntentName() {
    $(this).prop("disabled", true);

    if (inputValidation($("#inputIntentName").val(), 'intent_name')) {
        msgAlertIntent(2, 'Intent name need contain only the following: A-Z, a-z, 0-9 character');
        return false;
    }

    if(isNameExists($("#inputIntentName").val(),intents)){
        msgAlertIntent(2, 'Two identical Intent names are not allowed. Please choose a different name.');
        return false;
    }

    if (document.intentCreateForm.onsubmit)
        return false;

    RecursiveUnbind($('#wrapper'));
    document.intentCreateForm.submit();
}

function showIntents(str) {
    var wHTML = "";

    if (intents.length < 1) {
        msgAlertIntent(0, 'No intents yet. Create the first one');
        return;
    }
    else
        msgAlertIntent(0, 'Create an Intent to trigger your own business logic.');

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
        var blob = new Blob(["this file contains intent in JSON format"], {type: "text/plain;charset=utf-8;",});
        saveAs(blob, name + ".txt");
    }
    else {
        var blob = new Blob(["this file is a simulaion of CVS format file"], {type: "text/plain;charset=utf-8;",});
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

$(document).ready(function() {
    $(window).keydown(function(event){
        if( (event.keyCode == 13) && (postingIntentName() == false) ) {
            event.preventDefault();
            return false;
        }
    });
});