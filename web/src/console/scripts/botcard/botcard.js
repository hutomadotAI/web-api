function populateBotFields(botstoreItem, menu_title, carousel_category) {
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
    if (bot['lastUpdate'] != "") {
        var date = new Date(bot['lastUpdate']);
        dateString = date.toLocaleDateString() + " " + date.toLocaleTimeString();
    }
    document.getElementById('botLastUpdate').innerText = dateString;
    document.getElementById('botCategory').innerText = bot['category'];
    document.getElementById('botVersion').innerText = bot['version'];
    document.getElementById('botClassification').innerText = bot['classification'];
    document.getElementById('botPrivacyPolicy').setAttribute('href', checkLink(bot['privacyPolicy']));
    var botIconPath = '';
    if (bot['botIcon'] == null || bot['botIcon'] =='')
        botIconPath = BOT_ICON.DEFAULT_IMAGE.value;
    else
        botIconPath = BOT_ICON.PATH.value + bot['botIcon'];
    document.getElementById('botIcon').setAttribute('src', botIconPath);
    document.getElementById('botNamePurchase').innerText = bot['name'];
    document.getElementById('botDescriptionPurchase').innerText = bot['description'];
    document.getElementById('botPricePurchase').innerText = bot['price'];
    document.getElementById('botLicensePurchase').innerText = bot['licenseType'];
    document.getElementById('botIconPurchase').setAttribute('src', botIconPath);
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

    setButtonParameter(menu_title, JSON.parse(botstoreItem)['owned'], carousel_category)
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

function setButtonParameter(title, owned, carousel_category) {
    alert(title);
    var nodeCloseButtonBack = document.getElementById('btnBuyBotBack');
    var nodeButtonBack =  document.getElementById('btnBackToBotstore');
    switch (title) {
        case 'home' :
            var newAIBotstoreLink = './NewAIBotstore.php';

            if (owned)
                btnFromBuyToPurchased();
            if(carousel_category!='')
                newAIBotstoreLink += '?category=' + carousel_category;

            nodeCloseButtonBack.setAttribute('href', newAIBotstoreLink);
            nodeButtonBack.innerText = 'Go back';
            nodeButtonBack.setAttribute('href', newAIBotstoreLink);
            break;
        case 'settings' :
            nodeCloseButtonBack.setAttribute('href', './settingsAI.php?botstore=1');
            btnFromBuyToPurchased();
            break;
        case 'botstore' :
            var botstoreLink = './botstore.php';

            if (owned)
                btnFromBuyToPurchased();
            if(carousel_category!='')
                botstoreLink += '?category=' + carousel_category;

            nodeCloseButtonBack.setAttribute('href', botstoreLink);
            break;
        default:
            nodeCloseButtonBack.setAttribute('href', './botstore.php');
            nodeButtonBack.innerText = 'Go back';
            nodeButtonBack.setAttribute('href', '././botstore.php');
    }
}

$('#buyBot').on('hide.bs.modal', function (e) {
    var purchase_state = document.getElementById('purchase_state').value;
    if (purchase_state == 1)
        switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.BOTSTORE_FLOW.value);
});