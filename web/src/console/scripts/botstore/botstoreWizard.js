function populateBotFields(bot) {
    var json = JSON.parse(bot);
    document.getElementById('botTitle').innerText = json['name'];
    document.getElementById('botBadge').innerText = json['badge'];
    document.getElementById('botDescription').value = json['description'];
    if (json['alertMessage'] == null || json['alertMessage'].replace(/\s/g, "") == ''){
        document.getElementById('botMessageIcon').style.display = 'none';
        document.getElementById('botMessage').style.display = 'none';
    }
    else {
        document.getElementById('botMessage').value = json['alertMessage'];
    }
    document.getElementById('botLicense').innerText = json['licenseType'];
    document.getElementById('botPrice').innerText = json['price'];
    document.getElementById('botLongDescription').innerText = json['longDescription'];
    document.getElementById('botSample').innerText = json['sample'];
    var dateString = "";
    if (json['update'] != "") {
        var date = new Date(json['update']);
        dateString = date.toLocaleDateString() + " " + date.toLocaleTimeString();
    }
    document.getElementById('botLastUpdate').innerText = dateString;
    document.getElementById('botCategory').innerText = json['category'];
    document.getElementById('botVersion').innerText = json['version'];
    document.getElementById('botClassification').innerText = json['classification'];
    document.getElementById('botPrivacyPolicy').setAttribute('href', checkLink(json['privacyPolicy']));
    document.getElementById('botIcon').setAttribute('src', json['imagePath']);

    document.getElementById('botNamePurchase').innerText = json['name'];
    document.getElementById('botDescriptionPurchase').innerText = json['description'];
    document.getElementById('botPricePurchase').innerText = json['price'];
    document.getElementById('botLicensePurchase').innerText = json['licenseType'];
    document.getElementById('botIconPurchase').setAttribute('src', json['imagePath']);
    document.getElementById('bot_id').value = json['botId'];

    var dev = JSON.parse(devInfo);
    document.getElementById('botCompany').innerText = dev['company'];
    var elem = document.getElementById('developerInfo');

    if (dev['website'] == null || dev['website'] == '') {
        elem.style.display = 'none';
    } else {
        elem.style.display = 'block';
        document.getElementById('botWebsite').setAttribute('href', checkLink(dev['website']));
    }

    if (json['videoLink'] == null || videoLinkFilter(json['videoLink']) == '')
        document.getElementById('botVideoLinkSection').innerHTML = '';
    else
        document.getElementById('botVideoLink').setAttribute('src', videoLinkFilter(json['videoLink']));
}

function checkLink(link){
    if (link.indexOf('http') == -1 )
        link = 'http://' + link;
    return link;
}

function infoForBotstore(title, purchased) {
    var v = [];

    switch (title) {
        case 'home' :
            v['menu_title'] = title;
            v['menu_level'] = 0;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            if (purchased == 'true')
                btnFromBuyToPurchased();

            document.getElementById('btnBuyBotBack').setAttribute('href', './NewAIBotstore.php');
            document.getElementById('bthBackToBotstore').innerText = 'Go back';
            document.getElementById('bthBackToBotstore').setAttribute('href', './NewAIBotstore.php');

            break;
        case 'settings' :
            v['menu_title'] = title;
            v['menu_level'] = 1;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            document.getElementById('btnBuyBotBack').setAttribute('href', './settingsAI.php?botstore=1');


            btnFromBuyToPurchased();

            break;
        case 'botstore' :

            v['menu_title'] = 'botstore';
            v['menu_level'] = 2;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            if (purchased == 'true')
                btnFromBuyToPurchased();

            document.getElementById('btnBuyBotBack').setAttribute('href', './botstore.php');
    }
    return v;
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