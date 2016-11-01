document.getElementById('btnNext').addEventListener('click', wizardNext);
document.getElementById('ai_name').addEventListener('keyup', activeButtonCreate);
document.getElementById('ai_description').addEventListener('keyup', checkDescriptionLength);

function wizardNext() {
    document.getElementById('btnNext').setAttribute('disabled','disabled');
    document.getElementById('btnCancel').setAttribute('disabled','disabled');

    var value_name = document.getElementById('ai_name').value;
    if(inputValidation(value_name,'ai_name')) {
        msgAlertNameAI(2, 'Invalid name. Please enter a string that contains alphanumeric characters. No space are allowed.');
        document.getElementById('btnNext').setAttribute('disabled','disabled');
        document.getElementById('btnCancel').removeAttribute('disabled');
        inputsActiveDeactive(false);
        return;
    }

    var value_desc = document.getElementById('ai_description').value;
    if(inputValidation(value_desc,'ai_description') && value_desc.length > 0) {
        msgAlertDescriptionAI(2, 'Invalid description text. Please enter a string that contains alphanumeric characters.');
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
            msgAlertNameAI(1, 'Limit AI name reached.');
            break;
    }
}

function setConfidenceValueBeforePosting(){
    var element = document.getElementById('ai_confidence');
    var confidence_text = element.value;
    element.value = getValueFromConfidence(confidence_text);
}

function getValueFromConfidence(confidence_text){
    var values = {"never":0.0, "rarely":0.1, "sometimes":0.25, "often":0.4, "always":0.75};
    return values[confidence_text];
}

function checkDescriptionLength() {
    var limitTextInputSize = 100;
    if ( limitText($("#ai_description"), limitTextInputSize) == 1 )
        msgAlertDescriptionAI(1, 'Limit AI description reached.');
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
    if ( previousFilled == 1 ){
        resetGeneralInputFields();
        // active button next if are previous inserted data
        if ( document.getElementById('ai_name').value.length > 0 )
            document.getElementById('btnNext').removeAttribute('disabled');
        else
            document.getElementById('btnNext').setAttribute('disabled','disabled');
    }
    else {
        setSliderValue('ai_confidence', 0.0); // default value "never"
    }
});
