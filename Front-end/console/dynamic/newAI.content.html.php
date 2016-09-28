<div class="box box-solid box-clean flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <i class="fa fa-user-plus text-blue"></i>
        <h3 class="box-title">Create Your New AI</h3>
    </div>
    
    <form method="POST" name="createAIform" action="./domainsNewAI.php">
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

            <div class="row">
                <div class="col-md-12">
                    <?php include './dynamic/input.public.html.php'; ?>
                </div>
            </div>

    </form>

    <div class="box-footer">
            <a href="#" class="btn btn-primary flat" id="btnCancel" onClick="history.go(-1); return false;">cancel</a>
            <button type="submit" id="btnNext" class="btn btn-success flat" alt="next step">next</button>
    </div>
</div>

