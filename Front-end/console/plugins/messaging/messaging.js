
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
            break
        case 3:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break
        case 4:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break
    }
    document.getElementById('msgAlertNameAI').innerText = msg;
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
            break
        case 3:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break
        case 4:
            $("#containerMsgAlertUploadFile").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadFile").attr('class', 'icon fa fa-check');
            break
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
            break
        case 3:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break
        case 4:
            $("#containerMsgAlertUploadStructure").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadStructure").attr('class', 'icon fa fa-check');
            break
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
            break
        case 3:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break
        case 4:
            $("#containerMsgAlertUploadUrl").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertUploadUrl").attr('class', 'icon fa fa-check');
            break
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
            break
        case 3:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-success');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break
        case 4:
            $("#containerMsgAlertProgressBar").attr('class','alert alert-dismissable flat alert-primary');
            $("#iconAlertProgressBar").attr('class', 'icon fa fa-check');
            break
    }
    document.getElementById('msgAlertProgressBar').innerText = msg;
}

function closingMsgAlertProgressBarTemporized() {
    setTimeout(function(){ document.getElementById('containerMsgAlertProgressBar').style.display = 'none'; }, 6000);
}