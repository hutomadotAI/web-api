
    <div class="box-header with-border">
        <i class="fa fa-th text-red"></i>
        <div class="box-title"><b>Pre-training Neural Networks</b></div>
        <a data-toggle="collapse"  href="#collapseInfoDomains">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="boxDomains">
        <p></p>
        <input class="form-control flat no-shadow" id="searchInputDomains" value="" placeholder="This is a placeholder page. Please click next" tabindex="0" onkeyup="searchDomain(this.value)">
        <div class="form-group pull-right no-margin" style="padding-top: 5px;">
            <span style="padding-right:5px;">Show Free Neural Networks Only</span>
            <label>
                <input type="checkbox" class="minimal" checked>
            </label>
        </div>
        <br>
        <br>

      

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertNewDomains">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="icongAlertNewDomains"></i>
            <span id="msgAlertNewDomains">You can search and select one or more pre-training neural networks</span>
        </div>
    </div>
    
    <div id="collapseInfoDomains" class="panel-collapse collapse">
        <div class="box-body">
            <div class="overlay center-block">
                <section class="content-info" >
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
                need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
            </div>
        </div>
    </div>

    <div class="box-footer">
        <button type="submit" id="btnDomainsCancel" class="btn btn-primary flat">cancel</button>
        <button type="submit" id="btnSave" class="btn btn-success flat">save</button>
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