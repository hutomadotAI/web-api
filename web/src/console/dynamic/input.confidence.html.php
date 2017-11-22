<a data-toggle="collapse"  href="#collapseConfidence" tabindex="-1">
    <div class=" pull-right">more info
        <i class="fa fa-question-circle text-sm text-yellow"></i>
    </div>
</a>
<label for="ai_confidence">Let the bot create new answers</label>
<div class="box box-solid box-clean-fixed flat no-shadow" style=" background-color: #515151;">
    <div class="box-body flat">
        <div class="row margin">
            <div class="col-sm-12">
                <?php
                //  id = ai_confidence becomes the ionslider
                //  id = ai_confidence_param is set with the double value that the slider is set to
                //       immediately before the post fires.
                ?>
                <input type="hidden" name="ai_confidence_control" id="ai_confidence">
                <input type="hidden" name="ai_confidence" id="ai_confidence_param">
            </div>
        </div>
    </div>
</div>
<div id="collapseConfidence" class="panel-collapse collapse">
    <div class="box-body no-padding">
        <div class="overlay center-block">
            <section class="content-info" >
                <div class="box-body">
                    <dl class="dl-horizontal no-margin" style="text-align:justify">
                        By enabling this functionality you will let the Bot decide when to follow pre-packaged answers vs creating new ones based on the training data you provide.
                    </dl>
                </div>
            </section>
        </div>
    </div>
</div>


