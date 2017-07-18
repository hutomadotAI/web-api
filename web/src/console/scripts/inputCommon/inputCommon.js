/**
 * Created by Hutoma on 06/10/16.
 */

var languageLookup = {};
languageLookup['de-DE'] = 'Deutsch';
languageLookup['es-ES'] = 'Español';
languageLookup['fr-FR'] = 'Français';
languageLookup['it-IT'] = 'Italiano';
languageLookup['nl-NL'] = 'Nederlands';
languageLookup['pt-PT'] = 'Português';
languageLookup['en-US'] = 'English';

var languageReverseLookup = {};
languageReverseLookup['Deutsch'] = 'de-DE';
languageReverseLookup['Español'] = 'es-ES';
languageReverseLookup['Français'] = 'fr-FR';
languageReverseLookup['Italiano'] = 'it-IT';
languageReverseLookup['Nederlands'] = 'nl-NL';
languageReverseLookup['Português'] = 'pt-PT';
languageReverseLookup['English'] = 'en-US';

function resetGeneralInputFields(){
    var languageName = languageLookup[previousGeneralInfo.language];
    setInputValue('ai_name',previousGeneralInfo.name);
    setInputValue('ai_description',previousGeneralInfo.description);
    setSelectValue('ai_language',languageName);
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
        case (value < 0.1):
            confidence_index = 0;
            break;
        case (value < 0.3):
            confidence_index = 1;
            break;
        case (value < 0.45):
            confidence_index = 2;
            break;
        case (value < 0.55):
            confidence_index = 3;
            break;
        default:
            confidence_index = 4;
    }
    var slider = $('#'+id).data('ionRangeSlider');
    slider.update({
        from: confidence_index,
        disable: true
    });
}

function getValueFromConfidence(confidence_text){
    var values = {"never":0.0, "rarely":0.2, "sometimes":0.4, "often":0.5, "always":0.6};
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