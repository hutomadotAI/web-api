document.getElementById("btnAiSkillCancel").addEventListener("click", cancelAISkill);
document.getElementById("btnAiSkillSave").addEventListener("click", updateAISkill);


$(function () {
    if((purchasedBots).length==0) {
        msgAlertAiSkill(1,'This list is empty. Please go to the <a href="./botstore.php">botstore</a>');
        deactiveAiSkillButtons();
    }
});

function getLinkTasks(userSkill,aiSkill){
    var linkTasks = {};
    linkTasks.bots = new Array();

    for (var i = 0; i<userSkill.length; i++) {
        if(aiSkill.indexOf(parseInt(userSkill[i]['botId']))=== -1) {
            if (userSkill[i]['active']== 1) {
                //pushed to link a bot
                linkTasks.bots.push({
                    "botId" : userSkill[i]['botId'],
                    "active"  : '1'
                });
            }
        }
        else {
            if (userSkill[i]['active']== 0) { //exists
                //pushed to  unlink a bot
                linkTasks.bots.push({
                    "botId": userSkill[i]['botId'],
                    "active": '0'
                });
            }
        }
    }
    return linkTasks;

}

function updateAISkill() {
    deactiveAiSkillButtons();

    var userSkill = getUserActivities();
    var tasks = getLinkTasks(userSkill,linkedBots);
    var jsonString = JSON.stringify(tasks['bots']);

    if (jsonString.length<=2){   // character [] is empty request
        msgAlertAiSkill(1, 'Nothing is change and nothing to do');
        activeAiSkillButtons();
        return;
    }

    $.ajax({
        url : './dynamic/updateBotsLinked.php',
        type : 'POST',
        data: { 'aiSkill': jsonString },
        success: function (response) {
            var statusCode = JSON.parse(response);

            switch(statusCode['status']['code']){
                case 200:
                    msgAlertAiSkill(4, 'Your AI skill has been updated');
                    activeAiSkillButtons();
                    //TODO probably make difference to refresh data on redirection ( use POST not simple for messaging and cards BOT )
                    callback(jsonString);
                    break;
                case 404:
                    msgAlertAiSkill(2,'AI or Bot not found, or not currently linked');
                    activeAiSkillButtons();
                    break;
                case 500:
                    msgAlertAiSkill(2,'Try again. If the problem persists, contact us');
                    activeAiSkillButtons();
                    break;
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertAiSkill(2,'Something went wrong. Your changes were not saved.');
            activeAiSkillButtons();
        }
    });

}

function callback(jsonString){
 var task = JSON.parse(jsonString)
    for (var x in task){
        var botId = parseInt(task[x]['botId']);
        var active = task[x]['active'];
        var index = linkedBots.indexOf(botId);

        if(active == 0){
            if (index > -1)
                linkedBots.splice(index, 1);
        }
        else{
            if (index == -1)
                linkedBots.push(botId)
        }
    }
}


function getUserActivities(){
    var i, $list = $('.bot-linked');
    var userActivities=[];
    for (var i = 0; i<$list.length; i++) {
        var temp_item = $list.eq(i);
        var v = {};
        v['botId'] = temp_item.attr('data-botid');
        v['active'] = temp_item.attr('data-linked');
        userActivities.push(v);
    }
    return userActivities;
}

function cancelAISkill(){
    document.getElementById('tab_aiskill').className = "";
    document.getElementById('tab_general').className = "active";
    document.getElementById('page_aiskill').className = "tab-pane";
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
