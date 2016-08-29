<script src="./plugins/clipboard/clipboard.min.js"></script>

<div class="row">
<div class="col-md-12">
<div class="box box-solid box-clean flat no-shadow">

    <a data-toggle="collapse" data-parent="#accordion" href="#collapseKEYS">
        <div class="box-header with-border">
            <i class="fa fa-user-secret text-warning"></i>
            <h3 class="box-title">API keys</h3>
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
                        <span class="input-group-addon">Developer key</i></span>
                        <input type="text" class="form-control" id="devkey" value=" <?php echo(\hutoma\console::getDevToken());?>" disabled>
                        <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#devkey" id="devkeytooltip" title="copy to clipboard" onclick="copyToClipboard('devkey')" ><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>
            <p></p>
            <div class="row">
                <div class="col-md-12">
                    <div class="input-group">
                        <span class="input-group-addon">Client key</i></span>
                        <input type="text" class="form-control" id="clikey" value="<?php echo($_SESSION['dev_id']);?>" disabled>
                        <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#clikey" id="clikeytooltip" title="copy to clipboard" onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>
</div>
</div>