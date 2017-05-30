const MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL = 10;

window.addEventListener('resize', function () {
    if (document.getElementsByName('bot_list')[0].childElementCount > 0 ) {
        var node = document.getElementById('botsCarousels');
        var nCarousel = node.childElementCount;
        for (var i = 0; i < nCarousel; i++)
            showSeeMoreButton(node.children[i]);
    }
});

function showCarousel(botstoreCategorizedItems, category, optionFlow, see_more) {
    if (see_more === undefined) {
        see_more = false;
    }

    var targetElement = document.getElementById('botsCarousels');

    var wHTML = "";
    if (botstoreCategorizedItems === null) {
        if (category !== null) {
            wHTML += '<div class="carousel-box">';
            wHTML += '<div class="row no-margin" >';
            wHTML += '<span><div class="carousel-title pull-left"> ' + category + ' </div></span>';
            wHTML += '</div>';
            wHTML += ('<div class="row carousel-row no-margin carousel-overflow" name="bot_list">');
        }
        wHTML += "No bots are currently available.";
        var newNode = document.createElement('div');
        newNode.innerHTML = wHTML;
        targetElement.appendChild(newNode);
    } else {

        wHTML = "";
        wHTML += '<section class="carousel-content" style="padding-right: 0px;">';

        if (see_more) {
            wHTML += '<div class="">';
            wHTML += '<div class="row no-margin" >';
            wHTML += '<span><div class="carousel-title pull-left"> ' + category + ' </div></span>';
            wHTML += '</div>';
            wHTML += ('<div class="row no-margin" name="bot_list">');
        }
        else {
            wHTML += '<div class="carousel-box">';
            wHTML += '<div class="row no-margin" >';
            wHTML += '<span><div class="carousel-title pull-left"> ' + category + ' </div></span>';
            wHTML += '</div>';
            wHTML += ('<div class="row carousel-row no-margin carousel-overflow" name="bot_list">');
        }

        for (var x in botstoreCategorizedItems) {
            if (!botstoreCategorizedItems.hasOwnProperty(x)) {
                continue;
            }
            var bot = JSON.parse(botstoreCategorizedItems[x]);
            var botId = bot['metadata']['botId'];
            var botName = bot['metadata']['name'];
            var botDescription = bot['metadata']['description'];
            var botPrice = bot['metadata']['price'];
            var botCategory = bot['metadata']['category'];
            var botLicenseType = bot['metadata']['licenseType'];
            var botAuthor = bot['developer']['company'];
            var botOwned = bot['owned'];

            var openBotDetails = '';
            if (see_more)
                openBotDetails = 'onClick=openSingleBot(this,' + optionFlow + ',"' + botId + '","' + adjustURIEscapingCategoryValue(category) + '");';
            else
                openBotDetails = 'onClick=openSingleBot(this,' + optionFlow + ',"' + botId + '");';

            var botIconPath = '';
            if (!bot['metadata'].hasOwnProperty('botIcon') || bot['metadata']['botIcon'] === '')
                botIconPath = BOT_ICON.DEFAULT_IMAGE.value;
            else
                botIconPath = BOT_ICON.PATH.value + bot['metadata']['botIcon'];

            if (category.toLowerCase() === botCategory.toLowerCase()) {
                wHTML += '<span id="card' + botId + '" data-pos="' + x + '">';
                wHTML += '<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1">';
                // we need to have an href tag to allow crawlers to reach each bot's details
                wHTML += '<a href="/console/botcardDetail.php?botId=' + botId + '">';
                wHTML += '<img class="card-icon unselectable" src="' + botIconPath + '"' + openBotDetails + '>';
                wHTML += '</a>';
                wHTML += '<div class="card-title unselectable no-shadow"' + openBotDetails + '>';
                wHTML += botName;
                wHTML += '</div>';
                wHTML += '<div class="card-author unselectable no-shadow">';
                wHTML += 'by ' + botAuthor;
                wHTML += '</div>';

                wHTML += '<a class="card-testBotLink" id="cardTestBotLink' + botId + '" href="./newAI.php" target="_top">';
                wHTML += 'Test Bot';
                wHTML += '</a>';

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
                    + '" data-license="' + botLicenseType
                    + '" data-flow="' + optionFlow + '"';

                switch (optionFlow) {
                    case DRAW_BOTCARDS.BOTSTORE_WITH_BOT_FLOW.value:
                    case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:
                        wHTML += ('<span class="card-linked" data-botid = "' + botId + '" data-linked="">');
                        if (botOwned) {
                            /*wHTML += ('<div class="switch" id="btnSwitch' + botId
                             + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,'
                             + optionFlow + ',"' + botId + '"); data-link="0"></div>');*/
                            wHTML += ('<div class="card-purchased pull-right">');
                            wHTML += ('purchased');
                            wHTML += ('</div>');
                        }
                        else {
                            wHTML += ('<div class="card-price pull-right"' + dataBuyBot + '>');
                            wHTML += (botPrice + ' &#8364');
                            wHTML += ('</div>');
                        }
                        break;
                    case DRAW_BOTCARDS.BOTSTORE_FLOW.value:  // botstore showed in BOTSTORE
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
                        break;
                    default:
                        if (botOwned) {
                            wHTML += ('<div class="card-purchased pull-right">');
                            wHTML += ('purchased');
                            wHTML += ('</div>');
                        }
                        break;
                }

                wHTML += '</span>';
                wHTML += '</div>';
                wHTML += '</div>';
                wHTML += '</div>';
                wHTML += '</span>';
            }
        }

        wHTML += ('</div>');


        wHTML += ('<span class="carousel-see-more">');
        wHTML += ('<a href="botstoreList.php' + buildCategoryURIparameter(category) + '" onclick=\"triggerCategoryChanged(\''+ encodeURIComponent(category) + '\')\">');
        wHTML += ('<button class="btn btn-primary flat" value="' + category + '"><b>see more</b></button>');
        wHTML += ('</a></span>');
        wHTML += ('</div>');
        wHTML += ('</section>');

        var newNode = document.createElement('div');
        newNode.innerHTML = wHTML;
        targetElement.appendChild(newNode);

        showSeeMoreButton(newNode);
    }
}

function triggerCategoryChanged(category) {
    var event = new CustomEvent('BotstoreCategoryChanged', { detail : { 'category' : category } });
    window.parent.document.dispatchEvent(event);
}

function showSeeMoreButton(node) {
    var carouselBotcardNode = node.children[0].children[0].children[1];
    var firstBotcardNode = carouselBotcardNode.children[0].children[0];
    var marginBottom = window.getComputedStyle(firstBotcardNode).getPropertyValue("margin-bottom");

    var nodeSeeMore = node.children[0].children[0].lastChild;
    if ((parseInt(carouselBotcardNode.offsetHeight) + parseInt(marginBottom)) < parseInt(carouselBotcardNode.scrollHeight) || carouselBotcardNode.childElementCount >= MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL) {
        setSeeMoreButtonPosition(carouselBotcardNode, nodeSeeMore);
        nodeSeeMore.style.visibility = "visible";
    }
    else
        nodeSeeMore.style.visibility = "hidden";
}

function setSeeMoreButtonPosition(carouselBotcardNode, nodeSeeMore) {
    var nBot = carouselBotcardNode.childElementCount;
    var offsetTopPadding = parseInt(window.getComputedStyle(carouselBotcardNode).paddingTop);
    var offsetTopStartPosition = carouselBotcardNode.offsetTop + offsetTopPadding;
    for (var i = 0; i < nBot; i++) {
        var currNode = carouselBotcardNode.children[i].children[0];
        var offsetTopCurrentPosition = currNode.offsetTop;
        if (offsetTopCurrentPosition > offsetTopStartPosition) {
            var seeMoreWidth = nodeSeeMore.offsetWidth;
            var marginLeft = parseInt(window.getComputedStyle(currNode).marginLeft);
            nodeSeeMore.style.left = ( (currNode.offsetWidth + marginLeft ) * i ) - seeMoreWidth + 'px';
            break;
        }
    }
}

function getCarousels(category, optionFlow) {
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    jQuery.ajax({
        url: 'carousel.php',
        type: 'GET',
        data: {category: category},
        dataType: 'json',
        success: function (response) {
            var carouselsShown = 0;
            for (var key in response) {
                if (response.hasOwnProperty(key)) {
                    showCarousel(response[key], key, optionFlow, (category !== undefined && category !== ''));
                    carouselsShown++;
                }
            }
            if (carouselsShown === 0) {
                if (category !== "") {
                    var safe_category = htmlEncode(category);
                    showCarousel(null, safe_category, optionFlow, (safe_category !== undefined && safe_category !== ''));
                }
            }
            hideOverlay(true);
            // Notify any parent that we've finished painting
            window.parent.document.dispatchEvent(new CustomEvent('BotstoreFinishPaintEvent'));
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
        }
    });
}

function resizeIFrame() {
    var carouselBotcardListNode = document.getElementsByName('bot_list');
    if ( carouselBotcardListNode.length === 1 ){

        var carouselNode = carouselBotcardListNode[0];
        if ( carouselNode.childElementCount > 0 ) {
            var firstBotCarouselNode = carouselNode.children[0];
            var lastBotCarouselNode = carouselNode.lastChild;

            if (parseInt(lastBotCarouselNode.children[0].offsetTop) > parseInt(firstBotCarouselNode.children[0].offsetTop)) {
                var iFrame = parent.document.getElementById('contentFrame');
                iFrame.height = lastBotCarouselNode.children[0].offsetTop + lastBotCarouselNode.children[0].scrollHeight + 'px';
            }
        }
    }
}

function hideOverlay(state) {
    document.getElementById('carousel-overlay').style.display = (state) ? 'none' : '';
}

$(document).ready(function() {
    window.addEventListener('resize', function () {
        resizeIFrame();
    });
});
