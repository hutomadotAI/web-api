<form method="POST" id="domainsNweAIform" action="./saveAI.php" onsubmit="domainsToJsonForPOST()">
    <a href="#" class="btn btn-primary flat" id="btnBack" onClick="history.go(-1); return false;">back</a>
    <button type="submit" class="btn btn-success flat" id="btnSave" value="" onClick="">save</button>
    <p></p>

    <div class="input-group-btn">
        <input class="form-control input-lg " value="" placeholder="Search" tabindex="0" onkeyup="searchDomain(this.value)">
        <input type="hidden" id="userActivedDomains" name="userActivedDomains" value="">
    </div>
    <p></p>

    <h2></h2>
    <p id="domsearch"></p>
</form>
<p></p>