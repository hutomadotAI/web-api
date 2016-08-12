<form role="form">
    <div class="box-body">
        <div class="row">

            <!-- INPUT Name -->
            <div class="col-md-6">
                <div class="form-group">
                    <label for="ainame">Name</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="glyphicon glyphicon-user"></i>
                        </div>
                        <?php echo('<input type="text" class="form-control" name="ai_name" id="ai_name" placeholder="'.$_SESSION['ai_name'].'"disabled>');?>
                    </div>
                </div>
            </div>

            <!-- INPUT Language -->
            <div class="col-md-6">
                <?php include 'input.language.html.php'; ?>
            </div>
        </div>
        <div class="row">
            <!-- INPUT Description -->
            <div class="col-md-6">
                <div class="form-group">
                    <label for="aidesciption">Description</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="glyphicon glyphicon-pencil"></i>
                        </div>
                        <?php echo('<input type="text" class="form-control" name="ai_description" id="ai_description" placeholder="'.$_SESSION['ai_description'].'">'); ?>
                    </div>
                </div>
            </div>

            <!-- INPUT Time Zone -->
            <div class="col-md-6">
                <?php include './dynamic/input.timezone.html.php'; ?>
            </div>
        </div>

        <div class="row">

            <!-- Learn new Answer -->
            <div class="col-md-6">
                <a data-toggle="collapse"  href="#collapsePersonality">
                    <div class=" pull-right">more info
                        <i class="fa fa-question-circle text-md text-yellow"></i>
                    </div>
                </a>
                <label>Learn new answers</label>
                <div class="box box-solid box-clean-fixed flat no-shadow">
                    <div class="box-body">
                        <div class="row margin">
                            <div class="col-sm-12">
                                <input id="confidence" type="hidden" name="ai_confidence" id_confidence="ai_confidence" value="0;10">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- INPUT Personality -->
            <div class="col-md-6">
                <?php include './dynamic/input.personality.html.php'; ?>
                <?php include './dynamic/input.avatar.html.php'; ?>
            </div>
        </div>

        <div id="collapsePersonality" class="panel-collapse collapse">
            <div class="box-body">
                <div class="overlay center-block">
                    <section class="content bg-gray-light" >
                        <div class="box-body">
                            <dl class="dl-horizontal">
                                <dt>Description Actions</dt>
                                <dd>Tell the AI learning ...</dd>
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

        <h3><p class="text-muted">API key</p></h3>
        <div class="row">
            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-addon">Developer key</i></span>
                    <input type="text" class="form-control" id="devkey" value=" <?php echo(\hutoma\console::getDevToken());?>" disabled>
                    <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#devkey" id="devkeytooltip" title="copy to clipboard" onclick="copyToClipboard('devkey')" ><i class="fa fa-clipboard"></i></span>
                </div>
            </div>

            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-addon">Client key</i></span>
                    <input type="text" class="form-control" id="clikey" value="<?php echo($_SESSION['dev_id']);?>" disabled>
                    <span class="input-group-addon" data-clipboard-action="copy" data-toggle="tooltip"  data-clipboard-target="#clikey" id="clikeytooltip" title="copy to clipboard" onclick="copyToClipboard('clikey')"><i class="fa fa-clipboard"></i></span>
                </div>
            </div>
        </div>

    </div>
</form>
<script src="./plugins/clipboard/clipboard.min.js"></script>

<div class="box-footer">
      <a href="optionAI.php" class="btn btn-primary flat">cancel</a>
      <button  name="btnSave" value="btnSave" id="btnSave" value="_next" class="btn btn-success flat disabled" onClick="" alt="save">save</button>
</div>

