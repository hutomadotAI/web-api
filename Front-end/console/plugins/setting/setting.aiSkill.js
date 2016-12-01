//document.getElementById("btnAiSkillReset").addEventListener("click", resetAISkill);
document.getElementById("btnAiSkillCancel").addEventListener("click", cancelAISkill);
document.getElementById("btnAiSkillSave").addEventListener("click", updateAISkill);


$(function () {
    if(Object.keys(userActived).length==0) {
        msgAlertAiSkill(1,'This list is empty. Please go to the <a href="./botstore.php">botstore</a>');
        deactiveAiSkillButtons();
    }
});
function updateAISkill() {

    deactiveAiSkillButtons();
    //alert(JSON.stringify(userActived));
    msgAlertAiSkill(1,'Updating...');
    $.ajax({
        url : './dynamic/updateAIMesh.php',
        type : 'POST',
        data: { 'AiSkill': userActived },
        //dataType: 'json',
        //processData: false,  // tell jQuery not to process the data
        //contentType: "application/json; charset=utf-8",
        success: function (response) {
            var JSONdata = JSON.parse(response);
            var statusCode = JSONdata['status']['code'];

            if (statusCode === 200) {
                msgAlertAiSkill(4, 'Your AI skill has been updated');
                //updatePreviousDataLoaded(JSONdata);
                activeAiSkillButtons();
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertAiSkill(2,'Something went wrong. Your changes were not saved.');
            activeAiSkillButtons();
        }
    });
}

function resetAISkill(){
    var str='';
    document.getElementById('searchInputDomains').value = str;
    showDomains(str,1);
}

function cancelAISkill(){
    document.getElementById('tab_aiskill').className = "";
    document.getElementById('tab_general').className = "active";
    document.getElementById('page_aiskill').className = " tab-pane";
    document.getElementById('page_general').className = "tab-pane active";
}

function activeAiSkillButtons(){
    document.getElementById('btnAiSkillSave').removeAttribute('disabled');
    document.getElementById('btnAiSkillCancel').removeAttribute('disabled');
}

function deactiveAiSkillButtons(){
    document.getElementById('btnAiSkillSave').setAttribute('disabled','disabled');
    document.getElementById('btnAiSkillCancel').setAttribute('disabled','disabled');
}
