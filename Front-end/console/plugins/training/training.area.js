document.getElementById("inputfile").addEventListener("change", enableUploadFile);
document.getElementById("inputstructure").addEventListener("change", enableUploadStructure);
document.getElementById("inputurl").addEventListener("change", enableUploadUrl);

document.getElementById("btnUploadFile").addEventListener("click", uploadFile);
document.getElementById("btnUploadStructure").addEventListener("click", uploadStructure);
//document.getElementById("btnUploadUrl").addEventListener("click", uploadUrl);


function enableUploadFile() {
    $("#btnUploadFile").prop("disabled", false);
    msgAlert(0,'Now you can upload your file');
}

function enableUploadStructure() {
    $("#btnUploadStructure").prop("disabled", false);
    msgAlertStructure(0,'Now you can upload your complex file');
}

function enableUploadUrl() {
    var url = $("#inputurl").val();
    if ( learnRegExp(url) )
        $("#btnUploadUrl").prop("disabled", false);
    else
        $("#btnUploadUrl").prop("disabled", true);
}

function learnRegExp(url){
    return /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(url);
}

function uploadFile(){
    $("#btnUploadFile").prop("disabled", true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputfile", document.getElementById('inputfile').files[0]);
    file_data.append("tab","file");

    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','upload.php');

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            try {
                var JSONdata = JSON.parse(JSONresponse);
                if (JSONdata['status']['code'] === 200)
                    msgAlert(3,'File Uploaded!!!');
                else
                    msgAlert(2,'Something is gone wrong. File not uploaded');
            }catch (e){
                msgAlert(2,'Something is gone wrong. JSON error');
            }
            $("#btnUploadFile").prop("disabled", false);
        }
    };
    msgAlert(1,'Uploading file...');
    xmlhttp.send(file_data);
}


function uploadStructure(){
    $("#btnUploadStructure").prop("disabled", true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("inputstructure", document.getElementById('inputstructure').files[0]);
    file_data.append("tab","structure");

    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.open('POST','upload.php');

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            alert(JSONresponse);
            try {
                var JSONdata = JSON.parse(JSONresponse);
                if (JSONdata['status']['code'] === 200)
                    msgAlertStructure(3,'Complex file Uploaded!!!');
                else
                    msgAlertStructure(2,'Something is gone wrong. Complex file not uploaded');
            }catch (e){
                msgAlertStructure(2,'Something is gone wrong. JSON error on complex file transfer');
            }
            $("#btnUploadStructure").prop("disabled", false);
        }
    };
    msgAlertStructure(1,'Uploading complex file...');
    xmlhttp.send(file_data);
}




function msgAlert(alarm,msg){
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
    }
    document.getElementById('msgAlertUploadFile').innerText = msg;
}

function msgAlertStructure(alarm,msg){
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
    }
    document.getElementById('msgAlertUploadStructure').innerText = msg;
}

