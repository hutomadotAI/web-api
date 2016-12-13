<div class="box box-solid box-clean flat no-shadow" id="botstore">
    <div class="box-header with-border">
        <i class="fa fa-shopping-cart text-green"></i>
        <div class="box-title"><b>Botstore</b></div>
    </div>

    <div class="box-body" id="botstore">
        <input class="form-control flat no-shadow" value="" placeholder="Search the Bot Store..." tabindex="0" onkeyup="searchDomain(this.value)">
        <p></p>
        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertMarketplace" style="margin-bottom:5px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertMarketplace"></i>
            <span id="msgAlertMarketplace">You can power up your AI by combining and mixing existing AIs from out Bot Store.</span>
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
