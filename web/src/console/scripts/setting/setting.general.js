document.getElementById("btnReset").addEventListener("click", resetGeneralInputFields);
document.getElementById("btnSave").addEventListener("click", updateAI);
document.getElementById('ai_description').addEventListener('keydown', checkDescriptionLength);

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

    var formData = new FormData();
    
    formData.append('aiid', document.getElementById('aikey').value);
    formData.append('confidence', getValueFromConfidence(document.getElementById('ai_confidence').value));
    formData.append('name', document.getElementById('ai_name').value);
    formData.append('description', document.getElementById('ai_description').value);
    formData.append('language',document.getElementById('select2-' + 'ai_language' + '-container').innerHTML);
    formData.append('timezone',document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML);
    formData.append('personality',getSelectIndex('ai_personality'));
    formData.append('voice',getSelectIndex('ai_voice'));
    formData.append('default_chat_responses', document.getElementById('ai_default_response').value);

    msgAlertUpdateAI(ALERT.WARNING.value,'Updating...');
    $.ajax({
        url : './proxy/updateAI.php',
        type : 'POST',
        data : formData,
        //dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function (response) {
            var JSONdata = JSON.parse(response);
            var statusCode = JSONdata['status']['code'];
            if (statusCode === 200) {
                msgAlertUpdateAI(ALERT.SUCCESS.value, 'Your Bot\'s information has been updated.');
                updatePreviousDataLoaded(JSONdata);
                activeGeneralButtons();
            }
            else{
                msgAlertUpdateAI(ALERT.DANGER.value, JSONdata['status']['info']);
                activeGeneralButtons();
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertUpdateAI(ALERT.DANGER.value,'Whoops, something went wrong. Your changes weren\'t saved. Please retry');
            activeGeneralButtons();
        }
    });
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


function updatePreviousDataLoaded(JSONdata){
    var languageName = document.getElementById('select2-' + 'ai_language' + '-container').innerHTML;
    previousGeneralInfo.description =  document.getElementById('ai_description').value;
    previousGeneralInfo.language = languageReverseLookup[languageName];
    previousGeneralInfo.timezone = document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML;
    previousGeneralInfo.voice = document.getElementById('ai_voice').value;
    previousGeneralInfo.personality = getSelectIndex('ai_personality')
    previousGeneralInfo.confidence = getValueFromConfidence(document.getElementById('ai_confidence').value);
}

$(document).ready(function(){
    resetGeneralInputFields();
    $("#ai_name").prop("readonly",true);
});