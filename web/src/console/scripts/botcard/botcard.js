function displayBotCardDetail(botstoreItem, origin, carousel_category, flow) {

    var botcard_data = JSON.parse(botstoreItem);

    botcard_data.metadata['sample_html'] = function () {
        return this.metadata.sample.split('\n');
    };

    botcard_data['style_none_if_empty'] = function () {
        return function (val, render) {
            var message = render(val);
            return (!message || 0 === message.trim().length) ? 'display: none' : '';
        };
    };

    botcard_data['empty_if_undefined'] = function () {
        return function (val, render) {
            var message = render(val);
            return (!message || 0 === message.trim().length) ? '' : message;
        };
    };

    botcard_data['http_link'] = function () {
        return function (val, render) {
            var message = render(val).trim();
            if (message.length === 0) {
                return "";
            }
            var index = message.toLowerCase().indexOf('http');
            return (message.toLowerCase().indexOf('http') === 0) ?
                message : "http://" + message;
        };
    };

    botcard_data['get_bot_icon_url'] = function () {
        var message = this.metadata.botIcon.trim();
        return (message.length === 0) ?
            BOT_ICON.DEFAULT_IMAGE.value :
            BOT_ICON.PATH.value + message;
    }

    botcard_data.metadata['last_update_display'] = function () {
        var last_update = this.metadata.lastUpdate;
        if (!last_update || 0 === last_update.length) {
            return '';
        }
        var date = new Date(last_update);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    };

    botcard_data['youtube_id'] = function () {
        var url = this.metadata.videoLink;
        var regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
        var match = url.match(regExp);
        return (match && match[2].length == 11) ?
            match[2] : '';
    };

    botcard_data['coming_from'] = origin;

    botcard_data['back_button_link'] = function () {
        switch (this.coming_from) {
            case BOTCARD_DETAIL.SETTINGS:
                return './settingsAI.php?botstore=1';
            case BOTCARD_DETAIL.BOTSTORE:
                return 'javascript:history.back()';
            default:
                break;
        }
        return './botstore.php';
    }

    $.get('./templates/botcard_detail.mst', function (template) {
        $('#botcardDetailContent').replaceWith(Mustache.render(template, botcard_data));
        postRender(botcard_data, carousel_category, flow);
    });
}

function postRender(botcard_data, carousel_category, flow) {

    var owned = botcard_data.owned === true;

    var nodeButtonBack = document.getElementById('btnBackToBotstore');
    var nodeButtonBuy = document.getElementById('btnBuyBot');
    switch (botcard_data['coming_from']) {
        case BOTCARD_DETAIL.SETTINGS :
            btnFromBuyToPurchased();
            break;
        case BOTCARD_DETAIL.BOTSTORE :

            if (owned) {
                btnFromBuyToPurchased();
            } else {
                $('#btnBuyBot').click(purchaseBotFromBotcardDetail());
            }

            var botstoreLink = './botstore.php';
            if (carousel_category !== '') {
                botstoreLink += buildCategoryURIparameter(carousel_category);
            }

            document.getElementById('btnBuyBot').setAttribute('data-flow', (flow).toString());
            break;
        default:
            $('#btnBackToBotstore').innerText('Go back');
            $('#btnBackToBotstore').attr('href', 'javascript:history.back()');
            $('#btnBuyBot').click(function () {
                location.href = URLS.HUTOMA_CONSOLE;
            })
    }

    // Notify any parent that we've finished painting
    var cardHeight = $('#botcardDetailContent').outerHeight();
    if (window.parent !== null) {
        var event = new CustomEvent('BotstoreFinishPaintEvent', {
            detail: {
                height: cardHeight,
                event: 'BotstoreFinishPaintEvent'
            }
        });
        window.parent.postMessage(event.detail, '*');
    }
}