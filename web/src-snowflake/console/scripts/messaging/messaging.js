function msgAlertNameAI(alarm,msg){
    document.getElementById('containerMsgAlertNameAI').style.display = 'block';
    switch (alarm){
        case ALERT.BASIC.value:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "#d2d6de";
            break;
        case ALERT.WARNING.value:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "orange";
            break;
        case ALERT.DANGER.value:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_name').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertNameAI').innerText = msg;
}

function msgAlertDescriptionAI(alarm,msg){
    document.getElementById('containerMsgAlertDescriptionAI').style.display = 'block';
    switch (alarm){
        case ALERT.WARNING.value:
            $("#containerMsgAlertDescriptionAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertDescriptionAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_description').style.borderColor = "orange";
            break;
        case ALERT.DANGER.value:
            $("#containerMsgAlertDescriptionAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertDescriptionAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_description').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertDescriptionAI').innerText = msg;
}


function msgAlertUpdateAI(alarm,msg){
    document.getElementById('containerMsgAlertUpdateAI').style.display = 'block';
    switch (alarm){
        case ALERT.BASIC.value:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case ALERT.WARNING.value:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case ALERT.DANGER.value:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-warning');
            break;
        case ALERT.SUCCESS.value:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-success text-white');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case ALERT.PRIMARY.value:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertUpdateAI').innerText = msg;
}

function msgAlertAiSkill(alarm,msg){
    document.getElementById('containerMsgAlertAiSkill').style.display = 'block';
    switch (alarm){
        case ALERT.BASIC.value:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case ALERT.WARNING.value:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case ALERT.DANGER.value:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-warning');
            break;
        case ALERT.SUCCESS.value:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case ALERT.PRIMARY.value:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertAiSkill').innerHTML = msg;
}

function closingMsgAlertProgressBarTemporized() {
    setTimeout(function(){ document.getElementById('containerMsgAlertProgressBar').style.display = 'none'; }, 6000);
}