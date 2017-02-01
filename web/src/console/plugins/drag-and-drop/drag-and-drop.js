document.getElementById("drop-zone").addEventListener("dragover",function(e){
    e.preventDefault();
    this.className = 'upload-drop-zone drop';
});

document.getElementById("drop-zone").addEventListener("dragleave",function(e){
    e.preventDefault();
    this.className = 'upload-drop-zone';
});

document.getElementById("drop-zone").addEventListener("click",function(e){
    e.preventDefault();
    document.getElementById('inputfile').click();
});

document.getElementById("drop-zone").addEventListener("drop",function(e){
    e.preventDefault();
    this.className = 'upload-drop-zone';

    document.getElementById("message").innerText = 'image uploading...';
    loadFileIcon(e,true);
});

function loadFileIcon(e,option){
    var img = document.createElement('img');
    
    img.id = 'drop-zone';
    img.className = 'drag-icon';

    if(option) {
        document.getElementById('inputfile').files = e.dataTransfer.files;
        e.preventDefault();
    }
    else {
        img.file = e.target.files[0];
    }
    
    img.addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('inputfile').click();
    })

    var reader = new FileReader();
    reader.onload = (function (aImg) { return function (e) { aImg.src = e.target.result;}; })(img);

    if(option)
        reader.readAsDataURL(e.dataTransfer.files[0]);
    else
        reader.readAsDataURL( e.target.files[0]);
    
    document.getElementById('drop-icon').innerHTML = '';
    document.getElementById('drop-icon').appendChild(img);
}

