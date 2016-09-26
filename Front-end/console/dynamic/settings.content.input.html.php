<div class="box-header with-border">
    <i class="fa fa-sliders text-success"></i>
    <h3 class="box-title">General Informations</h3>
    <a data-toggle="collapse"  href="#collapseInfoGeneral">
        <div class=" pull-right">more info
            <i class="fa fa-question-circle text-md text-yellow"></i>
        </div>
    </a>
</div>

<div id="collapseInfoGeneral" class="panel-collapse collapse">
    <div class="box-body">
        <div class="overlay center-block">
            <section class="content-info" >
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description General aspectog AI</dt>
                        <dd>Before start training process, y.</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </section>
            <p></p>
            need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>
        </div>
    </div>
</div>


<form role="form">
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

        <h3><p class="text-muted">API key</p></h3>
        <div class="row">
            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-addon">Developer key</i></span>
                    <input type="text" class="form-control" id="devkey" value=" <?php echo \hutoma\console::getDevToken();?>" disabled>
                    <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#devkey" id="devkeytooltip" title="copy to clipboard" onclick="copyToClipboard('devkey')" ><i class="fa fa-clipboard"></i></span>
                </div>
            </div>

            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-addon">Client key</i></span>
                    <input type="text" class="form-control" id="clikey" value="<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['dev_id'];?>" disabled>
                    <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#clikey" id="clikeytooltip" title="copy to clipboard" onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>
        </div>

    </div>
</form>



<div class="box-footer">
    <button  name="btnCancel"  id="btnCancel" value="_cancel"   class="btn btn-primary flat" alt="cancel">cancel</button>
    <button  name="btnSave"  id="btnSave"   value="_next"   class="btn btn-success flat" alt="save" disabled>save</button>
    <button  name="btnDelete" id="btnDelete"  data-toggle="modal" data-target="#deleteAI" value="<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>" class="btn btn-danger flat pull-right" alt="delete">delete AI</button>
</div>



<!-- Modal DELETE AI-->
<div class="modal fade" id="deleteAI" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content bordered">
            <div class="modal-header">
                <button type="button" class="close" id="btnModelClose" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">   <i class="fa fa fa-warning text-danger" style="padding-right:2em"></i> DELETE AI</h4>
            </div>
            <div class="modal-body">
                <div class="box-body" id="delete-ai-label">

                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="./deleteAI.php">
                <button type="submit" class="btn btn-primary flat" id="modalDelete" data-dismiss="modal">Delete</button>
                <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel</button>
                </form>
            </div>
        </div>

    </div>
</div>