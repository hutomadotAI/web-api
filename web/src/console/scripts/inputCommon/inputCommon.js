/**
 * Created by Hutoma on 06/10/16.
 */
function resetGeneralInputFields(){
    setInputValue('ai_name',previousGeneralInfo.name);
    setInputValue('ai_description',previousGeneralInfo.description);
    setSelectValue('ai_language',previousGeneralInfo.language);
    setSelectValue('ai_timezone',previousGeneralInfo.timezone);
    setSelectByIndex('ai_voice',previousGeneralInfo.voice);
    setSelectByIndex('ai_personality',previousGeneralInfo.personality);
    setSliderValue('ai_confidence',previousGeneralInfo.confidence);
}

function setInputValue(id,value){
    var element = document.getElementById(id);
    element.value = value;
}

function setSelectByIndex(id,i) {
    var element = document.getElementById(id);
    element.selectedIndex = i;
    document.getElementById('select2-' + id + '-container').innerHTML = element.options[element.selectedIndex].text;
}

function setSelectValue(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

function getSelectIndex(id){
    return document.getElementById(id).selectedIndex;
}

function setSliderValue(id,value){
    var confidence_index;
    switch(true) {
        case (value < 0.05):
            confidence_index = 0;
            break;
        case (value < 0.15):
            confidence_index = 1;
            break;
        case (value < 0.25):
            confidence_index = 2;
            break;
        case (value < 0.6):
            confidence_index = 3;
            break;
        default:
            confidence_index = 4;
    }
    var slider = $('#'+id).data('ionRangeSlider');
    slider.update({
        from: confidence_index
    });
}

function getValueFromConfidence(confidence_text){
    var values = {"never":0.0, "rarely":0.1, "sometimes":0.2, "often":0.55, "always":0.7};
    return values[confidence_text];
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