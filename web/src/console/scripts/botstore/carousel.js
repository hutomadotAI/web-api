const MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL = 10;

window.addEventListener('resize', function () {
    notifyParentOfPaintEnd(document.getElementsByClassName('carousel-content').length === 0);
});

function showCarousel(botstoreCategorizedItems, category, see_more, showHeader, openFullStore) {
    var targetElement = $('#botsCarousels');

    if (botstoreCategorizedItems === null) {
        var view = {};
        if (showHeader) {
            view.showHeader = showHeader;
        }
        if (category !== null) {
            view.category = category;
        }
        $.get('templates/botstore_carousel_empty.mustache', function (template) {
            targetElement.html(Mustache.render(template, view));
        });
    } else {

        var safeCategory = encodeURIComponent(category);
        var categoryHash = getHash(category);

        $.get('templates/botstore_carousel_category.mustache', function (template) {
            var view = { carousel: {
                safeCategory: safeCategory,
                category: category,
                categoryHash: categoryHash
            }};
            if (showHeader) {
                view.carousel.showHeader = showHeader;
            }
            if (see_more) {
                view.carousel.showMoreButton = see_more;
            }
            targetElement.append(Mustache.render(template, view));


            $.get('templates/botstore_carousel_item.mustache', function (template) {
                var arr = [];
                for(var i = 0; i < botstoreCategorizedItems.length; i++) {
                    arr.push(JSON.parse(botstoreCategorizedItems[i]));
                }
                var view = { bots: arr
                    .map(function(bot, index) {
                        bot.index = index;
                        bot.openFullStore = openFullStore;
                        bot.safeCategory = safeCategory;
                        bot.iconPath = bot['metadata'].hasOwnProperty('botIcon')
                            ? BOT_ICON.PATH.value + bot.metadata.botIcon : BOT_ICON.DEFAULT_IMAGE.value;
                        bot.botOwnedBlock = bot.owned ? 'block' : 'none';
                        if (bot.owned) {
                            bot.purchased = true;
                        }
                        return bot;
                    })};
                $('#bot_list_' + categoryHash).html(Mustache.render(template, view));

                notifyParentOfPaintEnd(category === "");
            });
        });
    }
}

function triggerCategoryChanged(category) {
    if (window.parent !== null) {
        var event = new CustomEvent('BotstoreCategoryChanged', {
            detail: {
                category: category,
                event: 'BotstoreCategoryChanged'
            }
        });
        window.parent.postMessage(event.detail, '*');
    }
}

function getCarousels(category, showHeader, openFullStore) {
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    jQuery.ajax({
        url: 'carousel.php',
        type: 'GET',
        data: {category: category},
        dataType: 'json',
        success: function (response) {
            var carouselsShown = 0;
            var seeMoreButton = (category !== 'featured' && (category === undefined || category === ''));
            for (var key in response) {
                if (response.hasOwnProperty(key)) {
                    showCarousel(
                        response[key],
                        key,
                        seeMoreButton,
                        showHeader,
                        openFullStore);
                    carouselsShown++;
                }
            }
            if (carouselsShown === 0 && category !== "") {
                showCarousel(
                    null,
                    category,
                    seeMoreButton,
                    showHeader,
                    openFullStore);
            }
            hideOverlay(true);
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
        }
    });
}

function notifyParentOfPaintEnd(hasMultipleCategories) {
    // Notify any parent that we've finished painting
    var carousels = document.getElementsByClassName('carousel-content');
    if (carousels !== null && carousels.length > 0) {
        var lastCarousel = carousels[carousels.length - 1];
        var height;
        if (hasMultipleCategories) {
            height = lastCarousel.getBoundingClientRect().bottom;
        } else {
            var cards = lastCarousel.getElementsByClassName('box-card');
            if (cards.length > 0) {
                var lastCard = cards[cards.length - 1];
                height = lastCard.getBoundingClientRect().bottom;
            }
        }

        if (window.parent !== null) {
            var event = new CustomEvent('BotstoreFinishPaintEvent', {
                detail: {
                    height: height,
                    event: 'BotstoreFinishPaintEvent'
                }
            });
            window.parent.postMessage(event.detail, '*');
        }
    }
}

function hideOverlay(state) {
    document.getElementById('carousel-overlay').style.display = (state) ? 'none' : '';
}
