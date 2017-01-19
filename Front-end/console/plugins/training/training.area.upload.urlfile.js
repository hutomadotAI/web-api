function uploadUrl(){
    if ( $('#inputurl').val() == null ||  $('#inputurl').val() == "") {
        $("#btnUploadUrl").prop("disabled", true);
        msgAlertUploadStructure(ALERT.WARNING.value,'You need to choose a complex file first');
        return;
    }
    else
        $("#btnUploadUrl").prop("disabled", true);

    var xmlhttp;
    var file_data = new FormData();
    file_data.append("tab","url");

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
                    msgAlertUploadUrl(4,'Video Uploaded!!!');
                else
                    msgAlertUploadUrl(2,'Something has gone wrong. Video not uploaded');
            }catch (e){
                msgAlertUploadUrl(2,'Something has gone wrong. JSON error on video transfer');
            }
            $("#btnUploadUrl").prop("disabled", false);
        }
    };
    msgAlertUploadUrl(1,'Uploading Video...');
    xmlhttp.send(file_data);
}


function enableUploadUrl() {
    /* real implemented
     var url = $('#inputurl').val();
     if ( learnRegExp(url) )
     $('#btnUploadUrl').prop('disabled', false);
     else
     $("#btnUploadUrl").prop("disabled", true);
     */

    // for demo
    $("#btnUploadUrl").prop("disabled", true);
}
