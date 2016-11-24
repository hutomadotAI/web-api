<div class="box box-solid box-clean flat no-shadow" id="botstore">
    <div class="box-header with-border">
        <i class="fa fa-shopping-cart text-green"></i>
        <div class="box-title"><b>Bot Store</b></div>
        <a data-toggle="collapse"  href="#collapseInfoBotStore">
            <div class=" pull-right">more info
                <i class="fa fa-info-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>

    <div class="box-body" id="botstore">
        <?php include './dynamic/botstore.content.info.html.php'; ?>
        <input class="form-control flat no-shadow" value="" placeholder="Search the Bot Store..." tabindex="0" onkeyup="searchDomain(this.value)">
        <p></p>
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertMarketplace" style="margin-bottom:5px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertMarketplace"></i>
            <span id="msgAlertMarketplace">In this section bla bla bla bla bla bla bla bla bla bla bla.</span>
        </div>
    </div>

    <div id="collapseInfoBotStore" class="panel-collapse collapse">
        <div class="box-body no-margin">
            <div class="overlay center-block">
                <section class="content-info" >
                    <div class="box-body">
                        Pre-trained neural networks are made available by our community and provide out of the box knowledge to your AI so you don't have to start from scratch.
                    </div>
                </section>
                <p></p>
            </div>
        </div>
    </div>

    <div class="box-footer">
        <button  style="width:100px" type="submit" id="btnMarketplaceSave" class="btn btn-success flat pull-right"><b>save</b></button>
    </div>
</div>

<form method="POST" name="domainsNewAIform">
    <p></p>
    <h2></h2>
    <p id="domsearch"></p>
    <input type="hidden" name="userActivedDomains" id="userActivedDomains" val="" style="display:none;">
</form>


<!-- Modal DETAILS INFO-->
<div class="modal fade" id="detailsBotstore" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">AI Info</h4>
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
        </div>

    </div>
</div>