
<div class="box box-solid box-clean flat no-shadow no-margin"  id="jsonBox" style="position: absolute; bottom:0; z-index: -1;">
    <div class="box-header no-border text-gray text-center" style="background:#1e282c; color:#8A8A8A; height:35px; padding-top:10px;">
        <div class="box-title text-center text-gray" style="font-size: 12px;"> JSON Message</div>
    </div>

    <div id="collapseJSON" class="panel-collapse">
      <div class="box-body flat" style="bakcground: #212121; border-bottom: 1px solid #434343; border-top: 1px solid #434343; ">
          <pre id="msgJSON" class="text-gray no-border flat no-margin" style=" background-color: #515151; height: 65px;">
          </pre>
      </div>
      <div class="box-footer flat no-border">
          <div class="btn btn-success btn-sm center-block flat" id="btnJSON" data-toggle="tooltip" title="copy to clipboard" onclick="copyJsonToClipboard('result')" style="width: 100px;" >
              <i class="fa fa-copy" tabindex="-1" ></i> copy json
          </div>
      </div>
    </div>

</div>