document.getElementById("btnCancel").addEventListener("click", setInputFields);
document.getElementById("btnSave").addEventListener("click", updateAI);
document.getElementById("btnDomainsCancel").addEventListener("click", resetDomainsData);
document.getElementById('ai_description').addEventListener('keydown', checkDescriptionLength);

$(function () {
    $("#ai_confidence").ionRangeSlider({
        type: "single",
        grid: true,
        keyboard: true,
        onStart: function  (data) {console.log("onStart"); },
        onChange: function (data) {console.log("onChange"); },
        onFinish: function (data) {msgAlertUpdateAI(0, 'You can change main AI parameter and save it'); },
        onUpdate: function (data) {console.log("onUpdate"); },
        values: ["never", "rarely", "sometimes", "often", "always"]
    });
});

$(document).ready(function(){
    setInputFields();
    $("#ai_name").prop("disabled",true);
});

function resetDomainsData(){
    var str='';
    document.getElementById('searchInputDomains').value = str;
    showDomains(str,1);
}

function updateAI(){
    var xmlhttp;
    deactiveButtons();

    var input_data = new FormData();
    var is_private;
    if(document.getElementById('ai_public').value =='on')
        is_private = '0';
    else
        is_private = '1';

    input_data.append("private", is_private);
    input_data.append('aiid', document.getElementById('aikey').value);
    input_data.append('confidence', getValueFromConfidence(document.getElementById('ai_confidence').value));
    input_data.append('description', document.getElementById('ai_description').value);
    input_data.append('language',document.getElementById('select2-' + 'ai_language' + '-container').innerHTML);
    input_data.append('timezone',document.getElementById('select2-' + 'ai_timezone' + '-container').innerHTML);
    input_data.append('personality',getSelectIndex('ai_personality'));
    input_data.append('voice',getSelectIndex('ai_voice'));
    
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','./dynamic/updateAI.php');

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            var JSONdata = JSON.parse(JSONresponse);
            try {
                if (JSONdata['code'] === 200) {
                    msgAlertUpdateAI(4, 'Update AI successfull!');
                    updatePreviousDataLoaded(JSONdata);
                    activeButtons();
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

function updatePreviousDataLoaded(JSONdata){
    previousField.description = JSONdata.description;
    previousField.language = JSONdata.language;
    previousField.timezone = 'Europe/London';//JSONdata.timezone;
    previousField.voice = JSONdata.voice;
    previousField.personality = JSONdata.personality;
    previousField.confidence = JSONdata.confidence;
}
