<form method="POST" name="domainsAIform" action="./saveAI.php">
    <a href="#" class="btn btn-primary flat" id="btnBack" onClick="history.go(-1); return false;">back</a>
    <button type="submit" class="btn btn-success flat" id="btnNext" value="" onClick="">next</button>
    <p></p>

    <div class="input-group-btn">
        <input class="form-control input-lg " value="" placeholder="Search" tabindex="0" onkeyup="searchDomain(this.value)">
    </div>
    <p></p>

    <h2></h2>
    <p id="domsearch"></p>
</form>
<p></p>