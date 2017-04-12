function populateBotFields(botstoreItem, menu_title) {
    var bot = JSON.parse(botstoreItem)['metadata'];
    document.getElementById('botTitle').innerText = bot['name'];
    document.getElementById('botBadge').innerText = bot['badge'];
    document.getElementById('botDescription').value = bot['description'];
    if (bot['alertMessage'] == null || bot['alertMessage'].replace(/\s/g, "") == ''){
        document.getElementById('botMessageIcon').style.display = 'none';
        document.getElementById('botMessage').style.display = 'none';
    }
    else {
        document.getElementById('botMessage').value = bot['alertMessage'];
    }
    document.getElementById('botLicense').innerText = bot['licenseType'];
    document.getElementById('botPrice').innerText = bot['price'];
    document.getElementById('botLongDescription').innerText = bot['longDescription'];
    document.getElementById('botSample').innerText = bot['sample'];
    var dateString = "";
    if (bot['update'] != "") {
        var date = new Date(bot['update']);
        dateString = date.toLocaleDateString() + " " + date.toLocaleTimeString();
    }
    document.getElementById('botLastUpdate').innerText = dateString;
    document.getElementById('botCategory').innerText = bot['category'];
    document.getElementById('botVersion').innerText = bot['version'];
    document.getElementById('botClassification').innerText = bot['classification'];
    document.getElementById('botPrivacyPolicy').setAttribute('href', checkLink(bot['privacyPolicy']));
    document.getElementById('botIcon').setAttribute('src', ICON_PATH + bot['botIcon']);

    document.getElementById('botNamePurchase').innerText = bot['name'];
    document.getElementById('botDescriptionPurchase').innerText = bot['description'];
    document.getElementById('botPricePurchase').innerText = bot['price'];
    document.getElementById('botLicensePurchase').innerText = bot['licenseType'];
    document.getElementById('botIconPurchase').setAttribute('src', ICON_PATH + bot['botIcon']);
    document.getElementById('bot_id').value = bot['botId'];

    var dev = JSON.parse(botstoreItem)['developer'];
    document.getElementById('botCompany').innerText = dev['company'];
    var elem = document.getElementById('developerInfo');

    if (dev['website'] == null || dev['website'] == '') {
        elem.style.display = 'none';
    } else {
        elem.style.display = 'block';
        document.getElementById('botWebsite').setAttribute('href', checkLink(dev['website']));
    }

    if (bot['videoLink'] == null || videoLinkFilter(bot['videoLink']) == '')
        document.getElementById('botVideoLinkSection').innerHTML = '';
    else
        document.getElementById('botVideoLink').setAttribute('src', videoLinkFilter(bot['videoLink']));

    setButtonParameter(menu_title, JSON.parse(botstoreItem)['owned'])
}

function checkLink(link){
    if (link.indexOf('http') == -1 )
        link = 'http://' + link;
    return link;
}

function videoLinkFilter(url) {
    var src = '//www.youtube.com/embed/';
    var param = '?controls=1&hd=1&enablejsapi=1';
    url = url.replace(/\s/g, '');

    if (url == '')
        return '';

    if (url.indexOf('https://www.youtube.com') != -1) {
        var pos = url.lastIndexOf('=');
        if (pos == -1)
            pos = url.lastIndexOf("/");
        src += url.substring(pos + 1) + param;
        return src;
    }

    if (url.indexOf('https://youtu.be/') != -1) {
        var pos = url.lastIndexOf("/");
        src += url.substring(pos) + param;
        return src;
    }
    return '';
}

function setButtonParameter(title, owned) {
    switch (title) {
        case 'home' :
            if (owned)
                btnFromBuyToPurchased();
            document.getElementById('btnBuyBotBack').setAttribute('href', './NewAIBotstore.php');
            document.getElementById('bthBackToBotstore').innerText = 'Go back';
            document.getElementById('bthBackToBotstore').setAttribute('href', './NewAIBotstore.php');
            break;
        case 'settings' :
            document.getElementById('btnBuyBotBack').setAttribute('href', './settingsAI.php?botstore=1');
            btnFromBuyToPurchased();
            break;
        case 'botstore' :
            if (owned)
                btnFromBuyToPurchased();
            document.getElementById('btnBuyBotBack').setAttribute('href', './botstore.php');
            break;
        default:
            document.getElementById('btnBuyBotBack').setAttribute('href', './botstore.php');
            document.getElementById('bthBackToBotstore').innerText = 'Go back';
            document.getElementById('bthBackToBotstore').setAttribute('href', '././botstore.php');
    }
}

function btnFromBuyToPurchased() {
    var wHTML = '';
    var nodeBtn = document.getElementById('btnBuyBot');
    wHTML += ('<b>Bot purchased </b>');
    wHTML += ('<span class="fa fa-check-circle-o"></span>');
    nodeBtn.setAttribute('data-toggle', '');
    nodeBtn.setAttribute('data-target', '');
    nodeBtn.innerHTML = wHTML;
    nodeBtn.className = 'btn btn-primary pull-right flat';
}

$('#buyBot').on('hide.bs.modal', function (e) {
    var purchase_state = document.getElementById('purchase_state').value;
    if (purchase_state == 1)
        switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.BOTSTORE_FLOW.value);
});