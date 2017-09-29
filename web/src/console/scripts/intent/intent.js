document.getElementById("btnCreateIntent").addEventListener("click", postingIntentName);

function checkIntentCode(element, key) {
    if (key === 13) {
        if (activeButtonCreateIntent())
            postingIntentName();
    }
    else {
        activeButtonCreateIntent();
    }
}

function activeButtonCreateIntent() {
    var limitTextInputSize = 250;
    switch (limitText($("#inputIntentName"), limitTextInputSize)) {
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

    if (isNameExists($("#inputIntentName").val(), intents)) {
        msgAlertIntent(ALERT.DANGER.value, 'Intent name already exists. Please choose a different name.');
        return false;
    }

    submitElementClicked(inputIntentName.value);
    RecursiveUnbind($('#wrapper'));
}

function showIntents(filter) {
    if (intents.length < 1) {
        msgAlertIntent(ALERT.BASIC.value, 'No intents yet. Create the first one.');
        return;
    }
    else
        msgAlertIntent(ALERT.BASIC.value, 'Create an Intent to trigger your own business logic.');

    var view = {
        intents: intents.filter(function (item) {
            return !(filter.trim() !== "" && item.toLowerCase().indexOf(filter.toLowerCase()) === -1);
        }).map(function (intent, index) {
            return {name: intent, index: index};
        })
    };

    $.get('templates/intents_list.mustache', function(template) {
        $('#intentsearch').html(Mustache.render(template, view));
    });
}

function deleteIntent(elem) {
    var request = {
        url: './proxy/intentProxy.php?intent=' + intents[elem],
        verb: 'DELETE',
        onGenericError: function() {
            msgAlertIntentOp(ALERT.DANGER.value, "There was a problem deleting the intent.");
        },
        onOK: function(response) {
            msgAlertIntentOp(ALERT.SUCCESS.value, 'The intent was deleted.');
            // Should just delete the list entry, but for now refresh the page as previously
            location.href = 'intent.php';
        },
        onNotFound: function() {
            msgAlertIntentOp(ALERT.DANGER.value, 'Intent could not be found. Please refresh the page.');
        },
        onShowError: function(message) {
            msgAlertIntentOp(ALERT.DANGER.value, message);
        }
    };
    commonAjaxApiRequest(request);
}

function OnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function submitElementClicked(value) {
    var form = document.createElement('form');
    var element = document.createElement('input');

    form.method = 'POST';
    form.action = './intentelement.php';

    element.value = value;
    element.name = 'intent';
    element.setAttribute("type", "hidden");
    form.appendChild(element);
    document.body.appendChild(form);
    form.submit();
}

function editIntent(elem, intent) {
    elem.setAttribute('onclick', '');
    submitElementClicked(intent);
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

$( document ).ready(function() {
    if ( (trainingFile && intent_deleted) || ai_state === API_AI_STATE.STOPPED.value)
        createWarningIntentAlert(INTENT_ACTION.DELETE_INTENT.value);
    
});