document.getElementById('btnNext').addEventListener('click', wizardNext);
document.getElementById('ai_name').addEventListener('keydown', activeButtonCreate);
document.getElementById('ai_description').addEventListener('keydown', checkDescriptionLength);

function wizardNext() {
    document.getElementById('btnNext').setAttribute('disabled','disabled');
    document.getElementById('btnCancel').setAttribute('disabled','disabled');

    var value_name = document.getElementById('ai_name').value;
    if(isInputInvalid(value_name,'ai_name')) {
        msgAlertNameAI(ALERT.DANGER.value, 'Invalid name! Please enter a string that contains only alphanumeric characters.');
        document.getElementById('btnNext').setAttribute('disabled','disabled');
        document.getElementById('btnCancel').removeAttribute('disabled');
        inputsActiveDeactive(false);
        return;
    }
    
    if (isNameAlreadyExists(name_list, value_name)){
        msgAlertNameAI(ALERT.DANGER.value, 'Name already in use. Please choose another one.');
        return;
    }

    var value_desc = document.getElementById('ai_description').value;
    if(isInputInvalid(value_desc,'ai_description') && value_desc.length > 0) {
        msgAlertDescriptionAI(ALERT.DANGER.value, 'Invalid description! Please enter a string that contains only alphanumeric characters.');
        document.getElementById('btnNext').setAttribute('disabled','disabled');
        document.getElementById('btnCancel').removeAttribute('disabled');
        inputsActiveDeactive(false);
        return;
    }

    if(document.createAIform.onsubmit)
        return;

    RecursiveUnbind($('#wrapper'));
    setConfidenceValueBeforePosting();
    document.createAIform.submit();
}

function isNameAlreadyExists(name_list, value_name){
    for (var i=0; i<name_list.length; i++) {
        // remove multiple spaces
        var tmp_name = value_name.replace( /\s\s+/g, ' ' );
        // remove the last spaces
        tmp_name = tmp_name.replace(/\s+$/, '');
        if (tmp_name.toLowerCase() == name_list[i].toLowerCase())
            return true;
    }
    return false;
}

function activeButtonCreate() {
    var limitTextInputSize = 50;
    switch (limitText($("#ai_name"), limitTextInputSize)){
        case -1:
            document.getElementById('btnNext').setAttribute('disabled','disabled');
            break;
        case 0:
            document.getElementById('btnNext').removeAttribute('disabled');
            document.getElementById('containerMsgAlertNameAI').style.display = 'none';
            document.getElementById('ai_name').style.borderColor = "#d2d6de";
            break;
        case 1:
            msgAlertNameAI(ALERT.WARNING.value, 'Bot name\'s too long');
            break;
    }
}

function setConfidenceValueBeforePosting(){
    var element = document.getElementById('ai_confidence');
    var confidence_text = element.value;
    element.value = getValueFromConfidence(confidence_text);
}

function checkDescriptionLength() {
    var limitTextInputSize = 50;
    if ( limitText($("#ai_description"), limitTextInputSize) == 1 )
        msgAlertDescriptionAI(ALERT.WARNING.value, 'Bot description\'s too long');
    else {
        document.getElementById('btnNext').removeAttribute('disabled');
        document.getElementById('containerMsgAlertDescriptionAI').style.display = 'none';
        document.getElementById('ai_description').style.borderColor = "#d2d6de";
    }
}

$(function () {
    $("#ai_confidence").ionRangeSlider({
        type: "single",
        grid: true,
        keyboard: true,
        onStart: function (data) {console.log("onStart"); },
        onChange: function (data) {console.log("onChange"); },
        onFinish: function (data) { console.log("onFinish"); },
        onUpdate: function (data) {console.log("onUpdate"); },
        values: ["never", "rarely", "sometimes", "often", "always"]
    });
});

$(document).ready(function(){
    if ( previousFilled ){
        resetGeneralInputFields();
        // active button next if are previous inserted data
        if ( document.getElementById('ai_name').value.length > 0 )
            document.getElementById('btnNext').removeAttribute('disabled');
        else
            document.getElementById('btnNext').setAttribute('disabled','disabled');
    }
    else {
        setSliderValue('ai_confidence', 0.2); // default value "sometimes"
    }
    if(err)
        msgAlertNameAI(ALERT.DANGER.value, document.getElementById('ai_name').value+' name already in use. Please choose another one.');

});
