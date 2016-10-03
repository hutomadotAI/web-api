addEventListenOnChange();

function addEventListenOnChange(){
    document.getElementById('btnCancel').addEventListener('click', fillInputFields);
    document.getElementById('btnSave').addEventListener('click', updateAI);
    document.getElementById('btnDomainsCancel').addEventListener('click', resetDomainsData);

    document.getElementById("ai_description").addEventListener("keyup", active);

    document.getElementById("ai_language").addEventListener('change', active);
    document.getElementById("ai_timezone").addEventListener('change', active);
    document.getElementById("ai_personality").addEventListener('change', active);
    document.getElementById("ai_voice").addEventListener('change', active);
}

function active(){
    msgAlertUpdateAI(0,'You can change main AI parameter and save it')
    document.getElementById('btnSave').removeAttribute('disabled');
}

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



function updateAI(){
    var xmlhttp;
    deactiveButtons();

    var input_data = new FormData();
    input_data.append("aiid", document.getElementById('aikey').value);
    input_data.append("description", document.getElementById('ai_description').value);
    // hardcoded data passed

    var is_private;
    if(document.getElementById('ai_public').value =='on')
        is_private = '0';
    else
        is_private = '1';
    input_data.append("private", is_private);

    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','./dynamic/updateAI.php');

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var response = xmlhttp.responseText;
            try {
                if (response) {
                    msgAlertUpdateAI(4, 'Update AI successfull!');
                    activeButtons();
                    document.getElementById('btnSave').setAttribute('disabled','disabled');
                }
                else {
                    msgAlertUpdateAI(2, 'Upload AI failed');
                    activeButtons();
                }

            }catch (e){
                msgAlertUpdateAI(2,'Something is gone wrong during Upload process');
                activeButtons();
            }
        }
    };
    xmlhttp.send(input_data);
}

function activeButtons(){
    document.getElementById('btnSave').removeAttribute('disabled');
    document.getElementById('btnCancel').removeAttribute('disabled');
    document.getElementById('btnDelete').removeAttribute('disabled');
}

function deactiveButtons(){
    document.getElementById('btnSave').setAttribute('disabled','disabled');
    document.getElementById('btnCancel').setAttribute('disabled','disabled');
    document.getElementById('btnDelete').setAttribute('disabled','disabled');
}

