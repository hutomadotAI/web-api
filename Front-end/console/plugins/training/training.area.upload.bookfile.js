function uploadStructure(){
    if ( $('#inputstructure').val() == null ||  $('#inputstructure').val() == "") {
        $("#btnUploadStructure").prop("disabled", true);
        msgAlertUploadStructure(1,'Upload a page from a book to begin training');
        return;
    }

    if ( !checkFileSize('inputstructure',10) )
        return;

    resetPhaseTwoComponents();
    block_server_ping = true;
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
            try {
                var JSONdata = JSON.parse(JSONresponse);
                if (JSONdata['status']['code'] === 200) {
                    msgAlertUploadStructure(4, 'Complex file Uploaded!!!');
                    resetPhaseOneComponents();
                    updatePhaseOneComponents();
                }
                else{
                    msgAlertUploadStructure(2,'Something has gone wrong. Complex file not uploaded');
                    $("#btnUploadStructure").prop("disabled", false);
                }
            }catch (e){
                msgAlertUploadStructure(2,'Something has gone wrong; JSON error on complex file transfer');
                $("#btnUploadStructure").prop("disabled", false);
            }
        }
    };
    msgAlertUploadStructure(1,'Uploading complex file...');
    xmlhttp.send(file_data);
}

function enableUploadStructure() {
    // real implemented
    if ( $(this).val() == null || $(this).val == "")
        $("#btnUploadStructure").prop("disabled", true);
    else
        $("#btnUploadStructure").prop("disabled", false);
    msgAlertUploadStructure(0,'You can now upload your complex file');
}



function checkStructureSize(fileID,size) {
    var input, file;
    input = document.getElementById(fileID);
    if (!window.FileReader) {
        msgAlertUploadStructure(2,'The file API isn\'t supported on this browser');
        return false;
    }
    if (!input.files) {
        msgAlertUploadStructure(2,'This browser doesn\'t seem to support the \'files\' property of file inputs.');
        return false;
    }
    file = input.files[0];
    if(file.size > size*1048476) {
        msgAlertUploadStructure(2, 'The file size exceeds the limit allowed and cannot be uploaded.');
        return false;
    }
    return true;
}
