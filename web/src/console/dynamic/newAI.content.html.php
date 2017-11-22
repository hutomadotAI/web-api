<div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Basic Bot Settings</b></div>
    </div>
    <form method="POST" name="createAIform" action="./proxy/sessionCreateAI.php">
        <div class="box-body">

            <div class="alert alert-dismissable flat alert-danger no-margin" id="containerMsgAlertNewAI" style="display:none;">
                <button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>
                <i class="icon fa fa-warning" id="iconAlertNewAI"></i>
                <span id="msgAlertNewAI"></span>
            </div>

            <div class="row" style="margin-top: 10px;">
                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.name.html.php'; ?>
                </div>
                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.language.html.php'; ?>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.description.html.php'; ?>
                </div>
                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.timezone.html.php'; ?>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.confidence.html.php'; ?>
                </div>

                <div class="col-md-6">
                    <?php include __DIR__ . '/../dynamic/input.learn.html.php'; ?>
                    <?php include __DIR__ . '/../dynamic/input.voice.html.php'; ?>
                </div>

            </div>
        </div>
    </form>

    <div class="box-footer">
        <a href="#" style="width:100px" class="btn btn-primary flat" id="btnCancel" onClick="window.location.href='./home.php';"><b>Cancel</b></a>
        <button style="width:100px"  type="submit" id="btnNext" class="btn btn-success flat pull-right" alt="next step"><b>Next</b></button>
    </div>
    <p />
    <div class="box-header with-border">
        <div class="box-title"><b>Import Bot</b></div>
    </div>
    <div class="box-footer">
        <p>
            If you've previously exported a bot. You can re-import it here from the file. If the bot exported from exists, please change the name specified
            as a bot can't be created with a duplicate name.
        </p>
        <br />
        <form method="POST" action="./proxy/botImport.php" enctype="multipart/form-data" style="display:inline; margin:0; padding:0">
            <input type="file" name="file" required class="filestyle" data-iconName="glyphicon glyphicon-inbox"
                   data-buttonName="btn-success btn-sm flat" data-placeholder="Select Bot File"
                   data-buttonText="select bot file" style="float:left;">
        <b><input type="submit" value="Import Bot" class="btn btn-success flat pull-right" style="float:right;"></b>
        </form>
    </div>
</div>

