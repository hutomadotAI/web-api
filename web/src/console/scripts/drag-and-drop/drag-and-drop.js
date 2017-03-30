initializeEventListeners();

function loadFileIcon(e,option){
    e.stopPropagation();
    e.preventDefault();

    if(!isImageAllowed(e,option)){
        resetIcon();
        initializeEventListeners();
        return;
    }

    var img = document.createElement('img');
    img.id = 'drop-zone';
    img.className = 'drag-icon';

    if(!option)
        img.file = e.target.files[0];

    img.addEventListener('click', function(e) {
        e.stopPropagation();
        e.preventDefault();
        document.getElementById('inputfile').click();
    });

    img.addEventListener('dragover', function(e) {
        e.stopPropagation();
        e.preventDefault();
        img.className = 'drag-icon drag-over-opacity';
    });

    img.addEventListener('dragleave', function(e) {
        e.stopPropagation();
        e.preventDefault();
        img.className = 'drag-icon';
    });

    img.addEventListener('drop', function(e) {
        e.stopPropagation();
        e.preventDefault();
        loadFileIcon(e,true);
    });
    
    var reader = new FileReader();
    reader.onload = (function (aImg) { return function (e) { aImg.src = e.target.result; }; })(img);
    
    if(option)
        reader.readAsDataURL(e.dataTransfer.files[0]);
    else
        reader.readAsDataURL(e.target.files[0]);

    document.getElementById('drop-icon').innerHTML = '';
    document.getElementById('drop-icon').appendChild(img);
}

function isImageAllowed(e,option){
    var allowed = false;
    var imageSize = 512; //KBytes
    const allowedMimeTypes = ['image/jpeg','image/png']
    var dt;

    if(!option)
        dt = e.target;
    else
        dt = e.dataTransfer;

    var fileImage = dt.files[0];

    if(fileImage.size  > (imageSize*1024)){
        document.getElementById('msgAlertIconUpload').innerText = 'File size greater then ' + imageSize + ' KByte.';
        document.getElementById('containerMsgAlertIconUpload').classList.remove('hidden');
        return allowed;
    }

    if (allowedMimeTypes.indexOf(fileImage.type) >= 0)
        allowed = true;

    if(!allowed){
        document.getElementById('msgAlertIconUpload').innerText = 'Uploaded file is not a valid image. Only JPG, PNG files are allowed.';
        document.getElementById('containerMsgAlertIconUpload').classList.remove('hidden');
        return allowed;
    }

    document.getElementById('containerMsgAlertIconUpload').classList.add('hidden');
    return allowed;
}

function resetIcon(){
    var wHTML='';
    wHTML +='<span id="drop-icon">';
    wHTML +='<div class="col-xs-12 drag-icon" style="padding:10px;">';
    wHTML +='<div class="upload-drop-zone" id="drop-zone">';
    wHTML +='<i class="fa fa-file-image-o"></i>';
    wHTML +='<div class="text-sm unselectable" id="drag-info">Drag and drop a picture</div>';
    wHTML +='<div class="text-sm unselectable">210 x 185 pixel</div>';
    wHTML +='<div class="text-sm unselectable" id="message"></div>';
    wHTML +='</div>';
    wHTML +='</div>';
    wHTML +='</span>';
    document.getElementById('drop-icon').innerHTML = wHTML;
    document.getElementById('inputfile').value = "";
}

function initializeEventListeners(){
    document.getElementById("drop-zone").addEventListener("dragover",function(e){
        e.stopPropagation();
        e.preventDefault();
        this.className = 'upload-drop-zone drop';
    });

    document.getElementById("drop-zone").addEventListener("dragleave",function(e){
        e.stopPropagation();
        e.preventDefault();
        this.className = 'upload-drop-zone';
    });

    document.getElementById("drop-zone").addEventListener("click",function(e){
        e.stopPropagation();
        e.preventDefault();
        document.getElementById('inputfile').click();
    });

    document.getElementById("drop-zone").addEventListener("drop",function(e){
        e.stopPropagation();
        e.preventDefault();
        this.className = 'upload-drop-zone';

        document.getElementById("message").innerText = 'image uploading...';
        loadFileIcon(e,true);
    });
}