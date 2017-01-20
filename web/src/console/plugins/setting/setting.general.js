document.getElementById("btnReset").addEventListener("click", resetGeneralInputFields);
document.getElementById("btnSave").addEventListener("click", updateAI);
document.getElementById('ai_description').addEventListener('keydown', checkDescriptionLength);

$(function () {
    $("#ai_confidence").ionRangeSlider({
        type: "single",
        grid: true,
        keyboard: true,
        onStart: function  (data) {console.log("onStart"); },
        onChange: function (data) {console.log("onChange"); },
        onFinish: function (data) {msgAlertUpdateAI(0, 'Change your AI settings here.'); },
        onUpdate: function (data) {console.log("onUpdate"); },
        values: ["never", "rarely", "sometimes", "often", "always"]
    });
});

function updateAI() {

    deactiveGeneralButtons();

    var value_desc = document.getElementById('ai_description').value;
    if(inputValidation(value_desc,'ai_description') && value_desc.length > 0) {
        msgAlertDescriptionAI(ALERT.DANGER.value, 'Invalid description text. Please enter a string that contains alphanumeric characters.');
        document.getElementById('btnSave').setAttribute('disabled','disabled');
        document.getElementById('btnReset').removeAttribute('disabled');
        document.getElementById('btnDelete').removeAttribute('disabled');
        return;
    }

    var formData = new FormData();
    var is_private;

    if(document.getElementById('ai_public').value =='on')
        is_private = false;
    else
        is_private = true;

    formData.append('private', is_private);
    formData.append('aiid', document.getElementById('aikey').value);
    formData.append('confidence', getValueFromConfidence(document.getElementById('ai_confidence').value));
    formData.append('name', document.getElementById('ai_name').value);
    formData.append('description', document.getElementById('ai_description').value);
    formData.append('language',document.getElementById('select2-' + 'ai_language' + '-container').innerHTML);
    formData.append('timezone',document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML);
    formData.append('personality',getSelectIndex('ai_personality'));
    formData.append('voice',getSelectIndex('ai_voice'));

    msgAlertUpdateAI(1,'Updating...');
    $.ajax({
        url : './dynamic/updateAI.php',
        type : 'POST',
        data : formData,
        //dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function (response) {
            var JSONdata = JSON.parse(response);
            var statusCode = JSONdata['status']['code'];
            if (statusCode === 200) {
                msgAlertUpdateAI(4, 'Your AI has been updated');
                updatePreviousDataLoaded(JSONdata);
                activeGeneralButtons();
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertUpdateAI(2,'Something went wrong. Your changes were not saved.');
            activeGeneralButtons();
        }
    });
}

function checkDescriptionLength() {
    var limitTextInputSize = 100;
    if ( limitText($("#ai_description"), limitTextInputSize) == 1 )
        msgAlertDescriptionAI(ALERT.WARNING.value, 'Limit AI description reached.');
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
    var is_private;

    if(document.getElementById('ai_public').value =='on')
        is_private = false;
    else
        is_private = true;
    previousGeneralInfo.description =  document.getElementById('ai_description').value;
    previousGeneralInfo.language = document.getElementById('select2-' + 'ai_language' + '-container').innerHTML;
    previousGeneralInfo.timezone = document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML;
    previousGeneralInfo.voice = document.getElementById('ai_voice').value;
    previousGeneralInfo.personality = getSelectIndex('ai_personality')
    previousGeneralInfo.confidence = getValueFromConfidence(document.getElementById('ai_confidence').value);
    previousGeneralInfo.private = is_private;
}

$(document).ready(function(){
    resetGeneralInputFields();
    $("#ai_name").prop("disabled",true);
});