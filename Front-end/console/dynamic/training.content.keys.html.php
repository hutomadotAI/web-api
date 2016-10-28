<div class="row" id="box_keys">
<div class="col-md-12">
<div class="box box-solid box-clean flat no-shadow">

    <a data-toggle="collapse" data-parent="#accordion" href="#collapseKEYS">
        <div class="box-header with-border">
            <i class="fa fa-user-secret text-warning"></i>
            <div class="box-title"><b>API keys</b></div>
            <div class="box-tools pull-right">
                <button class="btn btn-box-tool" ><i class="fa fa-minus"></i></button>
            </div>
        </div>
    </a>

    <div id="collapseKEYS" class="panel-collapse collapse">
        <div class="box-body">

            <div class="row">
                <div class="col-md-12">
                    <div class="input-group">
                        <span class="input-group-addon text-gray" style="width:90px;">AI ID</i></span>
                        <input type="text" class="form-control flat no-shadow" id="aikey" value="<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'];?>" disabled>
                        <span class="input-group-addon text-gray" style="background-color: #2E3032;" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#aikey" id="aikeytooltip" title="copy to clipboard" onclick="copyToClipboard('aikey')" ><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>
            <p></p>
            <div class="row">
                <div class="col-md-12">
                    <div class="input-group">
                        <span class="input-group-addon text-gray" style="width:90px;">Dev Key</i></span>
                        <input type="text" class="form-control flat no-shadow" id="devkey" value="<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['dev_token'];?>" disabled>
                        <span class="input-group-addon text-gray" style="background-color: #2E3032; data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#devkey" id="devkeytooltip" title="copy to clipboard" onclick="copyToClipboard('devkey')"><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>
            <p></p>
            <div class="row">
                <div class="col-md-12">
                    <div class="input-group">
                        <span class="input-group-addon text-gray" style="width:90px;">Client Key</i></span>
                        <input type="text" class="form-control flat no-shadow" id="clikey" value="<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['client_token'];?>" disabled>
                        <span class="input-group-addon text-gray" style="background-color: #2E3032; data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#clikey" id="clikeytooltip" title="copy to clipboard" onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>
</div>
</div>