document.getElementById("btnModalCrop").addEventListener("click", cropImage);
document.getElementById("btnModelCancel").addEventListener("click", cropCancel);

function addImage(e) {
    var fd = new FormData();
    fd.append('file', e.dataTransfer.files[0]);
    var img = document.createElement('img');
    img.setAttribute('id','avatar');
    img.file = e.dataTransfer.files[0];

    document.getElementById('drop-zone-icon').className='fa fa-file-image-o';

    document.getElementById('drag-info').innerText = 'uploading '+ e.dataTransfer.files[0].name;

    var reader = new FileReader();
    reader.onload = (function (aImg) { return function (e) { aImg.src = e.target.result;}; })(img);
    reader.readAsDataURL(e.dataTransfer.files[0]);

    var item =  document.getElementById('imagePath');
    item.removeChild( item.firstChild );
    item.appendChild(img);
}

function cropImage(){
    document.getElementById('drag-square').style.backgroundImage =  "url('"+document.getElementById('avatar').getAttribute('src')+"')";
    document.getElementById('drop-zone').style.borderStyle = 'none';
    document.getElementById('drop-zone-icon').className='';
    document.getElementById('drag-info').innerText = '';

}

function cropCancel(){
    document.getElementById('drag-info').innerText = 'Just drag and drop an image file showing you AI here';
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

        document.getElementById('drag-square').style.backgroundImage = '';
        document.getElementById('drop-zone').style.borderStyle = 'dashed';

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

