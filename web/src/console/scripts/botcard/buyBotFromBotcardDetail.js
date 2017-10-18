/**
 * Create and send a Buy Skill Event to the GTM
 * @return {undefined}
 */
function createBuyBotEvent(eventname, name, aiid) {
    if ('dataLayer' in window) {
        dataLayer.push({
            event: 'abstractEvent',
            eventCategory: 'bot',
            eventAction: 'buy',
            eventLabel: eventname,
            eventMetadata: {
                timestamp: Date.now(),
                aiid: aiid,
                name: name
            }
        });
    }
}

function purchaseBotFromBotcardDetail() {
    var GENERIC_ERROR_STRING = "There was a problem acquiring the bot";
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    document.getElementById('btnBuyBot').disabled = true;
    var botId = document.getElementById('bot_id').value;
    $.ajax({
        url: './proxy/purchaseBot.php',
        data: { botId: botId },
        type: 'POST',
        success: function(response) {
            var parsedResponse = JSON.parse(response);
            var statusCode = parsedResponse['status']['code'];
            var message = parsedResponse['status']['info'];
            switch (statusCode) {
                case 200:
                    item = JSON.parse(botstoreItem)

                    createBuyBotEvent(item.metadata.aiid + "_" + user.email, user.email, item.metadata.aiid);
                    btnFromBuyToPurchased();
                    $('#store_purchase_confirmation_popup').modal('show');
                    break;
                default:
                    document.getElementById('msgAlertBotcardBox').style.display = 'block';
                    msgAlertBotcard(ALERT.DANGER.value, (message == null ? GENERIC_ERROR_STRING : message));
                    break;
            }
        },
        complete: function() {
            document.body.style.cursor = prevCursor;
            document.getElementById('btnBuyBot').disabled = false;
        },
        error: function() {
            document.getElementById('msgAlertBotcardBox').style.display = 'block';
            msgAlertBorcard(ALERT.DANGER.value, GENERIC_ERROR_STRING);
        }
    });
}