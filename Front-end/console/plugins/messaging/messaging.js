function msgAlertNameAI(alarm,msg){
    document.getElementById('containerMsgAlertNameAI').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_name').style.borderColor = "red";
            break;
        case 3:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertNameAI').innerText = msg;
}

function msgAlertDescriptionAI(alarm,msg){
    document.getElementById('containerMsgAlertDescriptionAI').style.display = 'block';
    switch (alarm){
        case 1:
            $("#containerMsgAlertDescriptionAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertDescriptionAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_description').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertDescriptionAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertDescriptionAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_description').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertDescriptionAI').innerText = msg;
}

function msgAlertUploadFile(alarm,msg){
    document.getElementById('containerMsgAlertUploadFile').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertUploadFile').innerText = msg;
}

function msgAlertUploadStructure(alarm,msg){
    document.getElementById('containerMsgAlertUploadStructure').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertUploadStructure').innerText = msg;
}

function msgAlertUploadUrl(alarm,msg){
    document.getElementById('containerMsgAlertUploadUrl').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertUploadUrl').innerText = msg;
}

function msgAlertProgressBar(alarm,msg){
    document.getElementById('containerMsgAlertProgressBar').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-success text-white');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertProgressBar').innerText = msg;
}

function msgAlertUpdateAI(alarm,msg){
    document.getElementById('containerMsgAlertUpdateAI').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertUpdateAI").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUpdateAI").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertUpdateAI').innerText = msg;
}

function msgAlertAiSkill(alarm,msg){
    document.getElementById('containerMsgAlertAiSkill').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-warning');
            break;
        case 3:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
        case 4:
            $("#containerMsgAlertAiSkill").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertAiSkill").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertAiSkill').innerHTML = msg;
}

function msgAlertEntity(alarm, msg) {
    switch (alarm) {
        case 0:
            $("#containerMsgAlertEntity").attr('class', 'alert alert-dismissable flat alert-base');
            $("#icongAlertEntity").attr('class', 'icon fa fa-check');
            document.getElementById('inputEntityName').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertEntity").attr('class', 'alert alert-dismissable flat alert-warning');
            $("#icongAlertEntity").attr('class', 'icon fa fa-check');
            document.getElementById('inputEntityName').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertEntity").attr('class', 'alert alert-dismissable flat alert-danger');
            $("#icongAlertEntity").attr('class', 'icon fa fa-warning');
            document.getElementById('inputEntityName').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertEntity').innerText = msg;
}


function msgAlertEntityValues(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertEntityValues").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertEntityValues").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertEntityValues").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertEntityValues").attr('class', 'icon fa fa-check');
            document.getElementById('value-entity').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertEntityValues").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertEntityValues").attr('class', 'icon fa fa-warning');
            document.getElementById('value-entity').style.borderColor = "red";
            break;
        case 4:
            $("#containerMsgAlertEntityValues").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertEntityValues").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertEntityValues').innerText = msg;
}

function msgAlertIntent(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertIntent").attr('class', 'icon fa fa-check');
            document.getElementById('inputIntentName').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertIntent").attr('class', 'icon fa fa-check');
            document.getElementById('inputIntentName').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertIntent").attr('class', 'icon fa fa-warning');
            document.getElementById('inputIntentName').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertIntent').innerText = msg;
}

function msgAlertIntentElement(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntentElement").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertIntentElement").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertIntentElement").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertIntentElement").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertIntentElement").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertIntentElement").attr('class', 'icon fa fa-warning');
            break;
        case 4:
            $("#containerMsgAlertIntentElement").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertIntentElement").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertIntentElement').innerText = msg;
}

function msgAlertUserExpression(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-check');
            document.getElementById('user-expression').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-check');
            document.getElementById('user-expression').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-warning');
            document.getElementById('user-expression').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertUserExpression').innerText = msg;
}

function msgAlertIntentVariable(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntentVariable").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertIntentVariable").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertIntentVariable").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertIntentVariable").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertIntentVariable").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertIntentVariable").attr('class', 'icon fa fa-warning');
            break;
    }
    document.getElementById('msgAlertIntentVariable').innerText = msg;
}

function msgAlertIntentResponse(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntentResponse").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertIntentResponse").attr('class', 'icon fa fa-check');
            document.getElementById('intent-response').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertIntentResponse").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertIntentResponse").attr('class', 'icon fa fa-check');
            document.getElementById('intent-response').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertIntentResponse").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertIntentResponse").attr('class', 'icon fa fa-warning');
            document.getElementById('intent-response').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertIntentResponse').innerText = msg;
}


function msgAlertIntentPrompt(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntentPrompt").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertIntentPrompt").attr('class', 'icon fa fa-check');
            document.getElementById('intent-prompt').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertIntentPrompt").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertIntentPrompt").attr('class', 'icon fa fa-check');
            document.getElementById('intent-prompt').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertIntentPrompt").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertIntentPrompt").attr('class', 'icon fa fa-warning');
            document.getElementById('intent-prompt').style.borderColor = "red";
            break;
    }
    document.getElementById('msgAlertIntentPrompt').innerText = msg;
}

function msgAlertMarketplace(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertMarketplace").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertMarketplace").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertMarketplace").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertMarketplace").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertMarketplace").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertMarketplace").attr('class', 'icon fa fa-warning');
            break;
        case 4:
            $("#containerMsgAlertMarketplace").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertMarketplace").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertMarketplace').innerText = msg;
}

function msgAlertPublish(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertPublish").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertPublish").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertPublish").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertPublish").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertPublish").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertPublish").attr('class', 'icon fa fa-warning');
            break;
        case 4:
            $("#containerMsgAlertPublish").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertPublish").attr('class', 'icon fa fa-check');
            break;
    }
    document.getElementById('msgAlertPublish').innerText = msg;
}

function closingMsgAlertProgressBarTemporized() {
    setTimeout(function(){ document.getElementById('containerMsgAlertProgressBar').style.display = 'none'; }, 6000);
}