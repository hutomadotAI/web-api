<div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Basic Bot Settings</b></div>
    </div>

    <form method="POST" name="createAIform" action="./dynamic/sessionCreateAI.php">
        <div class="box-body">

            <div class="alert alert-dismissable flat alert-danger no-margin" id="containerMsgAlertNewAI" style="display:none;">
                <button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>
                <i class="icon fa fa-warning" id="iconAlertNewAI"></i>
                <span id="msgAlertNewAI"></span>
            </div>

            <div class="row" style="margin-top: 10px;">
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
        </div>
    </form>

    <div class="box-footer">
        <a href="#" style="width:100px" class="btn btn-primary flat" id="btnCancel" onClick="window.location.href='./home.php';"><b>Cancel</b></a>
        <button style="width:100px"  type="submit" id="btnNext" class="btn btn-success flat pull-right" alt="next step"><b>Next</b></button>
    </div>
</div>

