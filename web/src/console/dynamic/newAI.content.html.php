<div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Basic AI Settings</b></div>
    </div>

    <form method="POST" name="createAIform" action="./NewAIBotstore.php">
        <div class="box-body">

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
        </div>
    </form>

    <div class="box-footer">
        <a href="#" style="width:100px" class="btn btn-primary flat" id="btnCancel" onClick="window.location.href='./home.php';"><b>Cancel</b></a>
        <button style="width:100px"  type="submit" id="btnNext" class="btn btn-success flat pull-right" alt="next step"><b>Next</b></button>
    </div>
</div>

