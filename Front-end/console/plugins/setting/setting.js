document.getElementById("btnCancel").addEventListener("click", fillInputFields);
document.getElementById("btnDomainsCancel").addEventListener("click", resetDomainsData);

$(function () {
    $(".select2").select2();
});

$(function () {

    $("#ai_confidence").ionRangeSlider({
        type: "single",
        min: 1,
        max: 4,
        from:2,
        from_value:"sometimes",
        step: 1,
        grid: true,
        keyboard: true,
        onStart: function (data) {console.log("onStart"); },
        onChange: function (data) {console.log("onChange"); },
        onFinish: function (data) { console.log("onFinish"); },
        onUpdate: function (data) {console.log("onUpdate"); },
        values: ["never", "sometimes", "often","always"]
    });
});



//Flat red color scheme for iCheck
$('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
    checkboxClass: 'icheckbox_flat-blue',
});


$(document).ready(function(){
    fillInputFields();
    $("#ai_name").prop("disabled",true);
});

function selectInputElement(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

function fillInputFields(){
    document.getElementById('ai_name').value = previousField.name;
    document.getElementById('ai_description').value = previousField.description;

    selectInputElement('ai_language',previousField.language);
    selectInputElement('ai_timezone',previousField.timezone);
    selectInputElement('ai_voice',previousField.voice);
    selectInputElement('ai_personality',previousField.personality);

    document.getElementById('ai_confidence').value = previousField.confidence;
    $("#ai_confidence").ionRangeSlider('upload');

    // enable/disable checkbox public AI

    if(previousField.private == '1') {
        $('input[type="checkbox"].flat-red').prop("checked",false)
    }
    else {
        $('input[type="checkbox"].flat-red').prop("checked", true);
    }
}

function resetDomainsData(){
    var str='';
    document.getElementById('searchInputDomains').value = str;
    showDomains(str,1);
}