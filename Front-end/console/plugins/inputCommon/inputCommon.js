/**
 * Created by Hutoma on 06/10/16.
 */
function setInputFields(){
    setInputValue('ai_name',previousField.name);
    setInputValue('ai_description',previousField.description);
    setCheckValue('ai_public',previousField.private);
    setSelectValue('ai_language',previousField.language);
    setSelectValue('ai_timezone',previousField.timezone);
    setSelectByIndex('ai_voice',previousField.voice);
    setSelectByIndex('ai_personality',previousField.personality);
    setSliderValue('ai_confidence',previousField.confidence);
}

function setInputValue(id,value){
    var element = document.getElementById(id);
    element.value = value;
}

function setSelectByIndex(id,i) {
    var element = document.getElementById(id);
    element.id[i].selected = true;
}


function setSelectValue(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

function setSliderValue(id,confidence){
    confidence = (confidence * 5) - 1;
    var slider = $('#'+id).data('ionRangeSlider');
    slider.update({
        from: confidence
    });
}

function setCheckValue(id,value){
    if(value == '1')
        $('#'+id).iCheck('uncheck');
    else
        $('#'+id).iCheck('check');
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

function inputsActiveDeactive(flag){
    $('#ai_description').prop('disabled',flag);
    $('#ai_confidence').prop('disabled',flag);
    $('#ai_timezone').prop('disabled',flag);
    $('#ai_voice').prop('disabled',flag);
    $('#ai_language').prop('disabled',flag);
    $('#ai_personality').prop('disabled',flag);
}

$(function () {
    $('.select2').select2();
});

$(document).ready(function(){
    $('input').iCheck({
        checkboxClass: 'icheckbox_square-blue'
    });
});
