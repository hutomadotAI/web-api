
+ function($) {
    // UPLOAD CLASS DEFINITION
    // ======================

    var dropZone = document.getElementById('drop-zone');
    var startUpload = function(files) {

        console.log('start upload');
        document.getElementById('drag-info').innerText = 'uploading';
        var formData = new FormData();
        formData.append("imageFile", files);

        $.ajax({
            url : './dynamic/uploadImage.php',
            type : 'POST',
            data : formData,
            dataType: 'json',
            processData: false,  // tell jQuery not to process the data
            contentType: false,  // tell jQuery not to set contentType
            success: function (response) {
                var JSONdata = response;
                var statusCode = JSONdata['status']['code'];

                if (statusCode === 200) {
                    var uploadWarnings = null;
                    var additionalInfo = JSONdata['status']['additionalInfo'];

                    if (additionalInfo != null)
                        uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);

                    if (uploadWarnings != null && uploadWarnings.length > 0)
                        document.getElementById('drag-info').innerText = 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n");
                    else
                        document.getElementById('drag-info').innerText = 'File uploaded';
                } else {
                    if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                        document.getElementById('drag-info').innerText = 'File not uploaded. No right content was found.';
                    } else {
                        document.getElementById('drag-info').innerText ='Something has gone wrong. File not uploaded.';
                    }
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {
                var JSONdata = JSON.stringify(xhr.responseText);
                document.getElementById('drag-info').innerText = 'Unexpected error occurred during upload';
            }
        });
    }

    /*
    dropZone.addEventListener('click', function(e) {
        document.getElementById('uploadfile').click();
        e.preventDefault()
        var uploadFiles = document.getElementById('uploadfile').files;
        startUpload(uploadFiles)
    })
    */

    dropZone.ondrop = function(e) {
        e.preventDefault();
        this.className = 'upload-drop-zone';
        startUpload(e.dataTransfer.files)
    }

    dropZone.ondragover = function() {
        this.className = 'upload-drop-zone drop';
        return false;
    }

    dropZone.ondragleave = function() {
        this.className = 'upload-drop-zone';
        return false;
    }

}(jQuery);