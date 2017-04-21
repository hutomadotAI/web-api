<div class="box box-solid box-clean flat no-shadow unselectable" id="botstore">
    <div class="box-header with-border">
        <i class="fa fa-shopping-cart text-green"></i>
        <div class="box-title"><b>Botstore</b></div>
    </div>

    <div class="box-body unselectable" id="botstore">
        <input class="form-control flat no-shadow" id="search-bot" value="" placeholder="Search the Bot Store..." tabindex="0"
               onkeyup="searchBots(this.value)">
        <p></p>

        <div class="alert alert-dismissable flat alert-base" id="containerMsgAlertMarketplace"
             style="margin-bottom:5px;">
            <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
            <i class="icon fa fa-check" id="iconAlertMarketplace"></i>
            <span id="msgAlertMarketplace">You can power up your AI by combining and mixing existing AIs from out Bot Store.</span>
        </div>
    </div>
</div>
   
<p></p>
<h2></h2>
<p id="botsSearch"></p>

<form method="POST" name="botsNewAIform">
    <input type="hidden" name="userActivedDomains" id="userActivedDomains" val="" style="display:none;">
</form>