function showBots(str, option) {
    var wHTML = "";
    for (var x in bots) {
        var bot = JSON.parse(bots[x]);


        if ((str != " ") && ( (str.length == 0) || (bot['name'].toLowerCase()).indexOf(str.toLowerCase()) != -1 )) {

            wHTML += ('<span id="card' + bot['botId'] + '" data-pos="' + x + '">');
            wHTML += ('<div class="col-lg-2 col-md-3 col-sm-4 col-xs-4">');
            //if($.inArray(bot['botId'], purchasedBots)!=-1)
            //    wHTML += ('<div class="box box-solid box-default-fixed flat borderActive" id="' + bot['aiid'] + '">');
            //else
            wHTML += ('<div class="box box-solid card flat" id="' + bot['aiid'] + '">');
            wHTML += ('<img class="card-icon" onClick=openSingleBot(this,"' + option + '","' + bot['botId'] + '",' + ($.inArray(bot['botId'], purchasedBots) != -1) + ');>');
            wHTML += ('<div class="card-title unselectable">' + bot['name'] + '</div>');
            wHTML += ('<div class="card-description unselectable" style="padding-left:5px;padding-right:5px;">' + bot['description'] + '</div>');
            wHTML += ('<div class="card-footer flat">');
            wHTML += ('<div class="card-link unselectable" onClick=openSingleBot(this,"' + option + '","' + bot['botId'] + '",' + ($.inArray(bot['botId'], purchasedBots) != -1) + '); >info and details</div>');
            wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="">');

            switch (option) {
                case 0:  // botstore showed during creation AI wizard
                    if ($.inArray(bot['botId'], purchasedBots) != -1) {
                        wHTML += ('<div class="switch" data-link="0" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"' + bot['botId'] + '","' + x + '");></div>');
                    }
                    else {
                        wHTML += ('<button class="btn btn-success center-block flat" id="btnBuyBot' + bot['botId'] + '" data-toggle="modal" data-target="#buyBot" data-botid="' + bot['botId'] + '" data-name="' + bot['name'] + '" data-description="' + bot['description'] + '" data-price="' + bot['price'] + '"style="width:130px;">');
                        wHTML += ('<b>Buy Bot </b>');
                        wHTML += ('<span class="fa fa-arrow-circle-right"></span>');
                        wHTML += ('</button>');
                    }
                    break;
                case 1:  // botstore showed in BOTSTORE
                    if ($.inArray(bot['botId'], purchasedBots) != -1) {
                        wHTML += ('<button class="btn btn-primary center-block flat" id="btnBuyBot' + bot['botId'] + '" data-toggle="" data-target="" data-botid="' + bot['botId'] + '" data-name="' + bot['name'] + '" data-description="' + bot['description'] + '" data-price="' + bot['price'] + '" style="width:130px;">');
                        wHTML += ('<b>Bot purchased </b>');
                        wHTML += ('<span class="fa fa-check-circle-o"></span>');
                        wHTML += ('</button>');
                    }
                    else {
                        wHTML += ('<button class="btn btn-success center-block flat" id="btnBuyBot' + bot['botId'] + '" data-toggle="modal" data-target="#buyBot" data-botid="' + bot['botId'] + '" data-name="' + bot['name'] + '" data-description="' + bot['description'] + '" data-price="' + bot['price'] + '" style="width:130px;">');
                        wHTML += ('<b>Buy Bot </b>');
                        wHTML += ('<span class="fa fa-arrow-circle-right"></span>');
                        wHTML += ('</button>');
                    }
                    break;
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

function addHtmlStarRating(actived, boxid, rating) {
    var wHTML = '';

    wHTML += ('<div class="flat">');
    wHTML += ('<div class="star-rating text-center">');
    wHTML += ('<div class="star-rating__wrap">');

    if (actived) {
        for (var i = 5; i > 0; i--) {
            if (i == Math.round(rating))
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" checked="checked">');
            else
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '">');
            wHTML += ('<label class="star-rating__ico fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>');
        }
    } else {
        // TODO if input is disable need add to input disabled="disabled" and in label icon __disabled - now the code is same
        for (var i = 5; i > 0; i--) {
            if (i == Math.round(rating))
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" checked="checked">');
            else
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '">');
            wHTML += ('<label class="star-rating__ico fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>');
        }
    }
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');
    return wHTML;
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
    if (option == 0)
        element.value = 'home';
    else
        element.value = 'botstore';

    element.type = 'hidden';
    form.appendChild(element);

    form.submit();
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


// on show Modal pass info to tiny purchsed process
$('#buyBot').on('show.bs.modal', function (e) {
    var curr_bot_id = $(e.relatedTarget).data('botid');
    var curr_bot_name = $(e.relatedTarget).data('name');
    var curr_bot_description = $(e.relatedTarget).data('description');
    var curr_bot_price = $(e.relatedTarget).data('price');

    document.getElementById('bot_id').value = curr_bot_id;
    document.getElementById('botNamePurchase').innerText = curr_bot_name;
    document.getElementById('botDescriptionPurchase').innerText = curr_bot_description;
    document.getElementById('botPricePurchase').innerText = curr_bot_price;
    document.getElementById('message').innerText = '';
    document.getElementById('btnPayment').disabled = false;
});

$('#buyBot').on('hide.bs.modal', function (e) {
    var purchase_state = document.getElementById('purchase_state').value;
    if (purchase_state == 1)
        switchCard(document.getElementById('bot_id').value);

});