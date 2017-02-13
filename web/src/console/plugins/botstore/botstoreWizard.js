function switchCard(botId) {
    var node = document.getElementById('card' + botId);
    var btnClassName = 'btn btn-success center-block flat'
    var pos = node.getAttribute('data-pos');
    var targetDiv = node.getElementsByClassName(btnClassName)[0];

    targetDiv.classList.remove("btn-success");
    targetDiv.classList.add("btn-primary");

    targetDiv.setAttribute('data-toggle', '');
    targetDiv.setAttribute('data-target', '');
    targetDiv.innerHTML = ('<b>Bot purchased </b><span class="fa fa-check-circle-o"></span>');

    //node.children[0].children[0].classList.add("borderActive");
}

function populateBotFields(bot) {
    var json = JSON.parse(bot);
    document.getElementById('botTitle').innerText = json['name'];
    document.getElementById('botBadge').innerText = json['badge'];
    document.getElementById('botDescription').value = json['description'];
    document.getElementById('botMessage').value = json['alertMessage'];
    document.getElementById('botLicense').innerText = json['licenseType'];
    document.getElementById('botPrice').innerText = json['price'];
    document.getElementById('botLongDescription').innerText = json['longDescription'];
    document.getElementById('botSample').innerText = json['sample'];
    document.getElementById('botLastUpdate').innerText = json['lastUpdate'];
    document.getElementById('botCategory').innerText = json['category'];
    document.getElementById('botVersion').innerText = json['version'];
    document.getElementById('botClassification').innerText = json['classification'];
    document.getElementById('botActivations').innerText = json['activations'];
    document.getElementById('botReport').setAttribute('href', json['report']);
    document.getElementById('botPrivacyPolicy').setAttribute('href', json['privacyPolicy']);
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
        document.getElementById('botWebsite').setAttribute('href', dev['website']);
    }

    if (json['videoLink'] == null || videoLinkFilter(json['videoLink']) == '')
        document.getElementById('botVideoLinkSection').innerHTML = '';
    else
        document.getElementById('botVideoLink').setAttribute('src', videoLinkFilter(json['videoLink']));
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

            document.getElementById('btnBuyBotBack').setAttribute('href', './NewAiBotstore.php');
            document.getElementById('bthBackToBotstore').innerText = 'Go back';
            document.getElementById('bthBackToBotstore').setAttribute('href', './NewAiBotstore.php');

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

