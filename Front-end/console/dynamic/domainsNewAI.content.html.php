<form method="POST" name="domainsNewAIform" action="./market.php">
    <div class="row row-centered">
        <div class="col-xs-4 col-centered">
            <a href="#" class="btn btn-primary flat" id="btnBack">back</a>
            <button type="submit" class="btn btn-success flat" id="btnNext">next</button>
        </div>

        <div class="col-xs-5 col-centered">
            <h4>Select one or more pre-training neural networks</h4>
        </div>

        <div class="col-xs-3 col-centered">
            <a data-toggle="modal"  data-target="#infoDomains" style="cursor: pointer;">
                <div class="pull-right" style="margin-top: 10px;">more info
                    <i class="fa fa-question-circle text-md text-yellow"></i>
                </div>
            </a>
        </div>
    </div>

   
    <p></p>
    <div class="input-group-btn">
    <input class="form-control input-lg " value="" placeholder="Search" tabindex="0" onkeyup="searchDomain(this.value)">
    </div>
    <p></p>
    <h2></h2>
    <p id="domsearch"></p>
    <input type="hidden" name="ai_name"         value="<?php echo $_POST['ai_name'] ?>">
    <input type="hidden" name="ai_description"  value="<?php echo $_POST['ai_description'] ?>">
    <input type="hidden" name="ai_language"     value="<?php echo $_POST['ai_language'] ?>">
    <input type="hidden" name="ai_timezone"     value="<?php echo $_POST['ai_timezone'] ?>">
    <input type="hidden" name="ai_confidence"   value="<?php echo $_POST['ai_confidence'] ?>">
    <input type="hidden" name="ai_personality"  value="<?php echo $_POST['ai_personality'] ?>">
    <input type="hidden" name="ai_sex"          value="<?php echo $_POST['ai_sex'] ?>">
    <input type="hidden" name="userActivedDomains" value="">
</form>

<p></p>






<!-- Modal INFO-->
<div class="modal fade" id="infoDomains" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">AI Domains</h4>
            </div>
            <div class="modal-body">
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
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>


<!-- Modal DETAILS INFO-->
<div class="modal fade" id="detailsDomain" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Details Domain</h4>
            </div>
            <div class="modal-body">
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
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>