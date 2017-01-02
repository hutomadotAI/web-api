function addImage(e) {
    var fd = new FormData();
    fd.append('file', e.dataTransfer.files[0]);
    var img = document.createElement('img');
    img.file = e.dataTransfer.files[0];

    document.getElementById('drag-info').innerText = 'uploading '+ e.dataTransfer.files[0].name;

    var reader = new FileReader();
    reader.onload = (function (aImg) { return function (e) { aImg.src = e.target.result;}; })(img);
    reader.readAsDataURL(e.dataTransfer.files[0]);

    var item =  document.getElementById('imagePath');
    item.removeChild( item.firstChild );
    item.appendChild(img);
}

+ function($) {
    var dropZone = document.getElementById('drop-zone');
    var startUpload = function(files) {
        document.getElementById('drag-info').innerText = 'uploading '+ files[0].name;
        document.getElementById('drag-info').setAttribute('class', 'text-sm unselectable flashing');
        var formData = new FormData();
        formData.append("uploadImageFile", files[0]);
        document.getElementById('imagePath').setAttribute('src', files[0].pathname);

        $('#cropIcon').modal('show');
        /*
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
         document.getElementById('drag-info').setAttribute('class', 'text-sm unselectable');
         addImage();

         },
         error: function (xhr, ajaxOptions, thrownError) {
         var JSONdata = JSON.stringify(xhr.responseText);
         document.getElementById('drag-info').setAttribute('class', 'text-sm unselectable');
         addImage();
         document.getElementById('drag-info').innerText = 'Sorry upload failed. Please try again. If the problem persists, contact our support team.';
         }
         });
         */

    }


    dropZone.addEventListener('click', function(e) {
        document.getElementById('uploadfile').click();
        e.preventDefault()
        var uploadFiles = document.getElementById('uploadfile').files;
        startUpload(uploadFiles)
    })


    dropZone.ondrop = function(e) {
        e.preventDefault();
        this.className = 'upload-drop-zone';

        addImage(e);

        $('#image-modal').modal('show');

        //startUpload(e.dataTransfer.files)
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


function handleFileUpload(files, obj) {
    for (var i = 0; i < files.length; i++) {
        var fd = new FormData();
        fd.append('file', files[i]);
        var status = new createStatusbar(obj); //Using this we can set progress.
        //status.setFileNameSize(files[i].name, files[i].size);
        //sendFileToServer(fd, status);

        var list = document.getElementById("image-list");
        var cell = document.createElement("td");
        var img = document.createElement("img");
        img.classList.add("obj");
        img.file = files[i];
        cell.setAttribute("align", "center");
        cell.setAttribute("valign", "bottom");
        cell.appendChild(img);
        list.appendChild(cell);

        var reader = new FileReader();
        reader.onload = (function (aImg) { return function (e) { aImg.src = e.target.result; }; })(img);
        reader.readAsDataURL(files[i]);
    }
}


$('#image-modal').on('show.bs.modal', function (e) {
    var image = document.querySelector('.drag-area > img');
    cropper = new Cropper(image, {
        dragMode: 'move',
        aspectRatio: 1 / 1,
        autoCropArea: 0.85,
        restore: false,
        guides: false,
        center: true,
        highlight: false,
        cropBoxMovable: false,
        cropBoxResizable: false,
        toggleDragModeOnDblclick: false,
    });

});

