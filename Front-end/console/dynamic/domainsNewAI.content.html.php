<div class="box box-solid box-clean flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <i class="fa fa-th text-red"></i>
        <h3 class="box-title">Pre-training Neural Networks</h3>
        <a data-toggle="collapse"  href="#collapseInfoNewDomains">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="boxNewDomains">
        <p></p>
        <input class="form-control " value="" placeholder="This is a non working page. Please click next" tabindex="0" onkeyup="searchDomain(this.value)">
        <div class="form-group pull-right no-margin" style="padding-top: 5px;">
            <span style="padding-right:5px;">Show Free Neural Networks Only</span>
            <label>
                <input type="checkbox" class="minimal" checked>
            </label>
        </div>
        <br>

        </br>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertNewDomains">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertNewDomains"></i>
            <span id="msgAlertNewDomains">You can search and select one or more pre-training neural networks</span>
        </div>
    </div>



    <div id="collapseInfoNewDomains" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content bg-gray-light" >
                    <div class="box-body">
                        <dl class="dl-horizontal">
                            <dt>Description New Domains Manipulation</dt>
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

    <div class="box-footer">
        <button type="submit" id="btnBack" class="btn btn-primary flat" alt="back step">back</button>
        <button type="submit" id="btnNext" class="btn btn-success flat" alt="next step">next</button>
    </div>
</div>



<form method="POST" name="domainsNewAIform" action="./dynamic/saveAI.php"><!-- across market page for demo -->
        <p></p>
        <h2></h2>
        <p id="domsearch"></p>
        <input type="hidden" name="userActivedDomains" value="">
</form>

<form method="POST" name="domainsNewAIformGoBack">
    <input type="hidden" name="prova" value="5">
</form>

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