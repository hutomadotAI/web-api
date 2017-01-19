<label for="ai_iconPath">Select file from your computer</label>
<span id="drop-icon">
    <div class="col-xs-12 drag-icon" style="padding:10px;">
        <div class="upload-drop-zone" id="drop-zone">
            <i class="fa fa-file-image-o"></i>
            <div class="text-sm unselectable" id="drag-info">Drag and drop a picture</div>
            <div class="text-sm unselectable">205 x 205 pixel</div>
              <div class="text-sm unselectable" id="message"></div>
        </div>
    </div>
</span>
<input type="file" name="inputfile" id="inputfile" style="display: none" onchange="loadFileIcon(event,false)">
