$(function () {
    $("#ai_confidence").ionRangeSlider({
        type: "single",
        grid: true,
        keyboard: true,
        onFinish: function () {msgAlertUpdateAI(ALERT.BASIC.value, 'This page allows you to change the basic information of your Bot.'); },
        values: ["never", "rarely", "sometimes", "often", "always"]
    });
});

function updateAI() {

    deactiveGeneralButtons();

    var value_desc = document.getElementById('ai_description').value;
    if(isInputInvalid(value_desc,'ai_description') && value_desc.length > 0) {
        msgAlertDescriptionAI(ALERT.DANGER.value, 'Description can contain only alphanumeric characters.');
        document.getElementById('btnSave').setAttribute('disabled','disabled');
        document.getElementById('btnReset').removeAttribute('disabled');
        document.getElementById('btnDelete').removeAttribute('disabled');
        return;
    }
    msgAlertUpdateAI(ALERT.WARNING.value,'Updating...');
    var request = {
        url: './proxy/aiProxy.php',
        data: {
            aiid: document.getElementById('aikey').value,
            confidence: getValueFromConfidence(document.getElementById('ai_confidence').value),
            name: document.getElementById('ai_name').value,
            description: document.getElementById('ai_description').value,
            language: document.getElementById('select2-' + 'ai_language' + '-container').innerHTML,
            timezone: document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML,
            personality: getSelectIndex('ai_personality'),
            voice: getSelectIndex('ai_voice'),
            default_chat_responses: document.getElementById('ai_default_response').value
        },
        verb: 'PUT',
        onGenericError: function(statusMessage) {
            var message = statusMessage === null
                ? 'Whoops, something went wrong. Your changes weren\'t saved. Please retry'
                : statusMessage;
            msgAlertUpdateAI(ALERT.DANGER.value, message);
            activeGeneralButtons();
        },
        onOK: function(response) {
            msgAlertUpdateAI(ALERT.SUCCESS.value, 'Your Bot\'s information has been updated.');
            updatePreviousDataLoaded();
            activeGeneralButtons();
        },
        onShowError: function(message) {
            msgAlertUpdateAI(ALERT.DANGER.value, message);
            activeGeneralButtons();
        }
    };
    commonAjaxApiRequest(request);
}

function checkDescriptionLength() {
    var limitTextInputSize = 100;
    if ( limitText($("#ai_description"), limitTextInputSize) === 1 )
        msgAlertDescriptionAI(ALERT.WARNING.value, 'Description is too long!');
    else {
        document.getElementById('btnSave').removeAttribute('disabled');
        document.getElementById('containerMsgAlertDescriptionAI').style.display = 'none';
        document.getElementById('ai_description').style.borderColor = "#d2d6de";
    }
}

function activeGeneralButtons(){
    document.getElementById('btnSave').removeAttribute('disabled');
    document.getElementById('btnReset').removeAttribute('disabled');
    document.getElementById('btnDelete').removeAttribute('disabled');
}

function deactiveGeneralButtons(){
    document.getElementById('btnSave').setAttribute('disabled','disabled');
    document.getElementById('btnReset').setAttribute('disabled','disabled');
    document.getElementById('btnDelete').setAttribute('disabled','disabled');
}


function updatePreviousDataLoaded(){
    var languageName = document.getElementById('select2-' + 'ai_language' + '-container').innerHTML;
    previousGeneralInfo.description =  document.getElementById('ai_description').value;
    previousGeneralInfo.language = languageReverseLookup[languageName];
    previousGeneralInfo.timezone = document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML;
    previousGeneralInfo.voice = document.getElementById('ai_voice').value;
    previousGeneralInfo.personality = getSelectIndex('ai_personality');
    previousGeneralInfo.confidence = getValueFromConfidence(document.getElementById('ai_confidence').value);
}

$(document).ready(function(){

    document.getElementById("btnReset").addEventListener("click", resetGeneralInputFields);
    document.getElementById("btnSave").addEventListener("click", updateAI);
    document.getElementById('ai_description').addEventListener('keydown', checkDescriptionLength);

    resetGeneralInputFields();
    $("#ai_name").prop("readonly",true);
});