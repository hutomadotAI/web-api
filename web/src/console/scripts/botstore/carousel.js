function showCarousel(botstoreCategorizedItems, category, option, see_more) {
    if(see_more === undefined )
        see_more = false;

    var wHTML = "";
    wHTML += '<section class="carousel-content" style="padding-right: 0px;">';
    wHTML += '<div class="carousel-box">';
    wHTML += '<div class="row no-margin" style="">';
    wHTML += '<span><div class="carousel-title pull-left"> ' + category + ' </div></span>';
    wHTML += '</div>';

    if (see_more)
        wHTML += ('<div class="row carousel-row no-margin" name="bot_list">');
    else
        wHTML += ('<div class="row carousel-row no-margin carousel-overflow" name="bot_list">');

    for (var x in botstoreCategorizedItems) {
        var bot = JSON.parse(botstoreCategorizedItems[x]);
        var botId = bot['metadata']['botId'];
        var botName = bot['metadata']['name'];
        var botDescription = bot['metadata']['description'];
        var botPrice = bot['metadata']['price'];
        var botCategory = bot['metadata']['category'];
        var botLicenseType = bot['metadata']['licenseType'];
        var botAuthor = bot['developer']['company'];
        var botOwned = bot['owned'];

        var openBotDetails= '';
        if (see_more)
            openBotDetails = 'onClick=openSingleBot(this,' + option + ',"' + botId + '","' + category + '");';
        else
            openBotDetails = 'onClick=openSingleBot(this,' + option + ',"' + botId + '");';

        var botIconPath = '';
        if (bot['metadata']['botIcon'] == null || bot['metadata']['botIcon'] =='')
            botIconPath = BOT_ICON.DEFAULT_IMAGE.value;
        else
            botIconPath = BOT_ICON.PATH.value + bot['metadata']['botIcon'];

        if ( category == botCategory) {
            wHTML += '<span id="card' + botId + '" data-pos="' + x + '">';
            wHTML += '<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1">';
            wHTML += '<img class="card-icon unselectable" src="' + botIconPath + '"' + openBotDetails + '>';
            wHTML += '<div class="card-title unselectable no-shadow"' + openBotDetails + '>';
            wHTML += botName;
            wHTML += '</div>';
            wHTML += '<div class="card-author unselectable no-shadow">';
            wHTML += 'by ' + botAuthor;
            wHTML += '</div>';
            wHTML += '<div class="card-footer flat unselectable">';
            wHTML += '<div class="row no-margin">';
            wHTML += '<div class="pull-left">';

            // TODO when API in ready we can add this infos
            //wHTML += '<i class="fa fa-star card-star"></i>';
            //wHTML += '<span class="card-users text-left">'+ bot['metadata']['activations']+'</span>';
            wHTML += '</div>';

            var dataBuyBot = 'id="btnBuyBot' + botId
                + '" data-toggle="modal" data-target="#buyBot" data-botid="' + botId
                + '" data-name="' + botName
                + '" data-description="' + botDescription
                + '" data-icon="' + botIconPath
                + '" data-price="' + botPrice
                + '" data-license="' + botLicenseType + '"';

            wHTML += ('<span class="card-linked" data-botid = "' + botId + '" data-linked="">');
            if (botOwned) {
                wHTML += ('<div class="card-purchased pull-right">');
                wHTML += ('purchased');
                wHTML += ('</div>');
            }
            else {
                wHTML += ('<div class="card-price pull-right"' + dataBuyBot + '>');
                wHTML += (botPrice + ' &#8364');
                wHTML += ('</div>');
            }

            wHTML += '</span>';
            wHTML += '</div>';
            wHTML += '</div>';
            wHTML += '</div>';
            wHTML += '</span>';
        }
    }
    wHTML += ('</div>');
    if (!see_more && botstoreCategorizedItems.length > MAX_BOTCARDS_VISIBLE_IN_BOTSORE)
        wHTML += '<span class="carousel-see-more pull-right"><button class="btn btn-primary flat" value="'+ category +'" onCLick="window.location.href=\'botstore.php?category='+ category +'\'";><b>see more</b></button></span>';
    wHTML += ('</div>');
    wHTML += ('</section>');

    var newNode = document.createElement('div');
    newNode.className = 'botsCarousel';
    newNode.innerHTML = wHTML;

    document.getElementById('botsCarousels').appendChild(newNode);
}

function getCarousels(category){
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    jQuery.ajax({
        url: 'carousel.php',
        type: 'GET',
        data: { category: category},
        dataType: 'json',
        success: function (response) {
            for (var key in response)
                showCarousel(response[key], key, DRAW_BOTCARDS.BOTSTORE_FLOW.value, category!==undefined);
            hideOverlay(true);
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
        }
    });
}

function hideOverlay(state) {
    document.getElementById('carousel-overlay').style.display = (state) ? 'none' : '';
}
