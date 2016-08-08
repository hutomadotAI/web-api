<div class="box box-solid box-clean flat no-shadow">
    <a data-toggle="collapse" data-parent="#accordion" href="#collapseKEYS">
        <div class="box-header with-border">
            <i class="fa fa-user-secret"></i>
            <h3 class="box-title">API keys</h3>
            <div class="box-tools pull-right">
                <button class="btn btn-box-tool" ><i class="fa fa-minus"></i></button>
            </div>
        </div>
    </a>

    <div id="collapseKEYS" class="panel-collapse collapse">
        <div class="box-body">

            <div class="row">
                <div class="col-md-6">
                    <div class="input-group">
                        <span class="input-group-addon">Developer key</i></span>
                        <input type="text" class="form-control" id="devkey" placeholder=" <?php echo(\hutoma\console::getDevToken());?>" disabled>
                        <span class="input-group-addon" data-toggle="tooltip"  id="devkeytooltip" title="copy to clipboard" onclick="copyToClipboard('devkey')" ><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="input-group">
                        <span class="input-group-addon">Client key</i></span>
                        <input type="text" class="form-control" id="clikey" placeholder="<?php echo($_SESSION['dev_id']);?>" disabled>
                        <span class="input-group-addon" data-toggle="tooltip"  id="clikeytooltip" title="copy to clipboard" onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>


        </div>
    </div>
</div>


