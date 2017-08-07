<script>
    var previousGeneralInfo = <?php echo json_encode($aiInfo); ?>;
    function regenerateHmacSecret() {
        var errorMessage = 'There was a problem updating the webhook secret. Please try again later and if the issue ' +
            'persists, please contact support.'
        $.ajax({
            url: './dynamic/webhook.regenerate.secret.php',
            type: 'POST',
            success: function (response) {
                var parsedResponse = JSON.parse(response);
                if (parsedResponse === null) {
                    msgAlertUpdateAI(ALERT.DANGER.value,errorMessage);
                }
                else {
                    var statusCode = parsedResponse['status']['code'];
                    var message = parsedResponse['status']['info'];
                    switch (statusCode) {
                        case 200:
                            var input = document.getElementById('webhook_secret');
                            input.value = message;
                            msgAlertUpdateAI(ALERT.SUCCESS.value, 'The webhook secret has been updated');
                            break;
                        default:
                            msgAlertUpdateAI(ALERT.DANGER.value,errorMessage);
                            break;
                    }
                }
            },
            complete: function () {
            },
            error: function () {
                msgAlertUpdateAI(ALERT.DANGER.value,errorMessage);
            }
        });
    }
</script>


<form role="form">

    <div class="modal-content bordered" style="padding:10px;background-color: #202020">
        <div class="row">
            <div class="col-md-6">
                <?php include './dynamic/input.name.html.php'; ?>
            </div>
            <div class="col-md-6">
                <?php include './dynamic/input.language.html.php'; ?>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <?php include './dynamic/input.description.html.php'; ?>
            </div>
            <div class="col-md-6">
                <?php include './dynamic/input.timezone.html.php'; ?>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <?php include './dynamic/input.confidence.html.php'; ?>
            </div>

            <div class="col-md-6">
                <?php include './dynamic/input.learn.html.php'; ?>
                <?php include './dynamic/input.voice.html.php'; ?>
            </div>

        </div>

        <h3><p class="text-muted">API keys</p></h3>
        <div class="row">
            <div class="col-md-6">
                <div class="input-group" style="padding-bottom:10px;">
                    <span class="input-group-addon text-gray" style="width:90px;">Bot ID</i></span>
                    <input type="text" class="form-control flat no-shadow" id="aikey"
                           value="<?php echo $aiInfo['aiid']; ?>"
                           readonly>
                    <span class="input-group-addon text-gray" data-clipboard-action="copy" data-toggle="tooltip"
                          data-clipboard-target="#aikey" id="aikeytooltip" title="copy to clipboard"
                          onclick="copyToClipboard('aikey')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>

            <div class="col-md-6">
                <div class="input-group" style="padding-bottom:10px;">
                    <span class="input-group-addon text-gray" style="width:90px;">Dev key</i></span>
                    <input type="text" class="form-control flat no-shadow" id="devkey"
                           value="<?php echo \hutoma\console::getDevToken(); ?>"
                           readonly>
                    <span class="input-group-addon text-gray" data-clipboard-action="copy" data-toggle="tooltip"
                          data-clipboard-target="#devkey" id="devkeytooltip" title="copy to clipboard"
                          onclick="copyToClipboard('devkey')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6" style="padding-bottom:10px;">
                <div class="input-group">
                    <span class="input-group-addon text-gray" style="width:90px;">Client key</i></span>
                    <input type="text" class="form-control flat no-shadow" id="clikey"
                           value="<?php echo $aiInfo['client_token']; ?>"
                           readonly>
                    <span class="input-group-addon text-gray" data-clipboard-action="copy" data-toggle="tooltip"
                          data-clipboard-target="#clikey" id="clikeytooltip" title="copy to clipboard"
                          onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>

            <div class="col-md-6" style="padding-bottom:10px;">
                <div class="input-group">
                    <span class="input-group-addon text-gray" style="width:90px;">Webhook signing secret</i></span>
                    <input type="text" class="form-control flat no-shadow" id="webhook_secret"
                           value="<?php echo $aiInfo['hmac_secret']; ?>"
                           readonly>
                    <span class="input-group-addon text-gray"  data-toggle="modal" data-target="#regenHmacSecret"
                          id="webhook_secret_regen_tooltip" title="re-generate secret"
                          onclick=""><i class="fa fa-refresh"></i></span>
                    <span class="input-group-addon text-gray" data-clipboard-action="copy" data-toggle="tooltip"
                          data-clipboard-target="#webhook_secret" id="webhook_secret_copy_tooltip" title="copy to clipboard"
                          onclick="copyToClipboard('webhook_secret')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>
        </div>

        <p></p>
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertUpdateAI">
            <i class="icon fa fa-check" id="iconAlertUploadUrl"></i>
            <span id="msgAlertUpdateAI">This page allows you to change the basic information of your bot.</span>
        </div>

    </div>
</form>


<div class="box-footer unselectable">
    <button name="btnCancel" id="btnReset" value="_cancel" class="btn btn-primary flat">revert to saved values</button>
    <button name="btnSave" id="btnSave" value="_save" class="btn btn-success flat">save</button>
    <button name="btnDelete" id="btnDelete" data-toggle="modal" data-target="#deleteAI"
            value="<?php echo $aiInfo['name']; ?>"
            class="btn btn-danger flat pull-right" alt="delete">delete Bot
    </button>
</div>


<!-- Modal DELETE AI-->
<div class="modal fade" id="deleteAI" role="dialog">
    <div class="modal-dialog flat"  style="border: 1px solid red;">
        <!-- Modal content-->
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa fa-warning text-danger" style="padding-right:2em"></i> DELETE BOT
                </h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="delete-ai-label">

                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="./dynamic/deleteai.php">
                    <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-danger flat" id="modalDelete" data-dismiss="modal">Delete
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>

<!-- Modal Regenerate HMAC secret-->
<div class="modal fade" id="regenHmacSecret" role="dialog">
    <div class="modal-dialog flat"  style="border: 1px solid red;">
        <!-- Modal content-->
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa fa-warning text-danger" style="padding-right:2em"></i> Regenerate the webhook
                    signing secret
                </h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="delete-ai-label">
                    Warning - if this is updated, all code relying on the existing key for signature
                    validation must be updated.
                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="regenForm">
                    <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-danger flat" id="modalDelete" data-dismiss="modal"
                            onclick="regenerateHmacSecret()">
                        Regenerate
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>
