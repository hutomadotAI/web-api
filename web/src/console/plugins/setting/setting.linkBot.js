function showBots(str, option) {
    var wHTML = "";
    for (var x in purchasedBots) {
        var bot = JSON.parse(purchasedBots[x]);

        if ((str != " ") && ( (str.length == 0) || (bot['name'].toLowerCase()).indexOf(str.toLowerCase()) != -1 )) {

            wHTML += ('<span id="card' + bot['botId'] + '" data-pos="' + x + '">');
            wHTML += ('<div class="col-lg-2 col-md-3 col-sm-4 col-xs-4">');
            if ($.inArray(bot['botId'], linkedBots) != -1)
                wHTML += ('<div class="box box-solid card flat borderActive" id="' + bot['aiid'] + '">');
            else
                wHTML += ('<div class="box box-solid card flat" id="' + bot['botId'] + '">');
            wHTML += ('<img class="card-icon" src="' + bot['imagePath'] + '" onClick=openSingleBot(this,"' + option + '","' + bot['botId'] + '",' + ($.inArray(bot['botId'], purchasedBots) != -1) + '); ><i class="' + bot['imagePath'] + '"></i></img>');
            wHTML += ('<input type="text" class="form-control flat no-shadow no-margin card-title unselectable"  value="' + bot['name'] + '" readonly style="padding-bottom:0">');
            wHTML += ('<input type="text" class="form-control flat no-shadow card-description unselectable" value="' + bot['description'] + '" style="padding: 0 5px 5px 5px" readonly>');
            wHTML += ('<div class="card-footer flat">');
            wHTML += ('<div class="card-link unselectable" onClick=openSingleBot(this,"' + option + '","' + bot['botId'] + '",' + ($.inArray(bot['botId'], purchasedBots) != -1) + '); >more info</div>');
            if ($.inArray(bot['botId'], linkedBots) != -1) {
                wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="1">');
                wHTML += ('<div class="switch switchOn" data-link="1" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=switchClick(this,"' + bot['botId'] + '","' + x + '");></div>');
            } else {
                wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="0">');
                wHTML += ('<div class="switch"  data-link="0" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=switchClick(this,"' + bot['botId'] + '");></div>');
            }
            wHTML += ('</span>');
            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</span>');
        }
    }
    newNode.innerHTML = wHTML;
    document.getElementById('botsSearch').appendChild(newNode);
}


function switchClick(node, botId, pos) {
    var parent = node.parentNode;

    $(node).toggleClass('switchOn');
    if ($(node).attr('data-link') == '0') {

        $(node).attr('data-link', 1);
        parent.setAttribute('data-linked', '1');
        document.getElementById('card' + botId).children[0].children[0].classList.add("borderActive");
    }
    else {
        $(node).attr('data-link', 0);
        parent.setAttribute('data-linked', '0');
        document.getElementById('card' + botId).children[0].children[0].classList.remove("borderActive");
    }
}


function openSingleBot(elem, option, botId, purchased) {
    elem.setAttribute('onClick', '');

    var form = document.createElement("form");
    document.body.appendChild(form);
    form.method = "POST";
    form.action = "./dynamic/sessionBotMenu.php";

    var element = document.createElement("INPUT");
    element.name = "botId";
    element.value = botId;
    element.type = 'hidden';
    form.appendChild(element);

    var element = document.createElement("INPUT");
    element.name = "purchased";
    element.value = purchased;
    element.type = 'hidden';
    form.appendChild(element);

    var element = document.createElement("INPUT");
    element.name = "menu_title";
    element.value = 'settings';
    element.type = 'hidden';

    form.appendChild(element);

    form.submit();
}


function activeRightMenu(response) {
    if (response == '1') {
        document.getElementById('page_general').classList.remove('active');
        document.getElementById('page_aiskill').classList.add('active');
        document.getElementById('tab_general').className = '';
        document.getElementById('tab_aiskill').className = ('active');
    }
}