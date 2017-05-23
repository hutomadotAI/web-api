document.getElementById("btnPayment").addEventListener("click", purchaseBot);

function purchaseBot() {
    var flow = JSON.parse(this.getAttribute('data-flow'));
    switch(flow) {
        case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value :
        case DRAW_BOTCARDS.BOTSTORE_WITH_BOT_FLOW.value :
            var GENERIC_ERROR_STRING = "There was a problem acquiring the bot";
            var prevCursor = document.body.style.cursor;
            document.body.style.cursor = 'wait';
            document.getElementById('btnPayment').disabled = true;
            var botId = document.getElementById('bot_id').value;
            document.getElementById('message').innerText = 'Sending request...';
            $.ajax({
                url: './dynamic/purchaseBot.php',
                data: {botId: botId},
                type: 'POST',
                success: function (response) {
                    try {
                        var parsedResponse = JSON.parse(response);
                        var statusCode = parsedResponse['status']['code'];
                        var message = parsedResponse['status']['info'];
                        switch (statusCode) {
                            case 200:
                                document.getElementById('message').innerText = 'Bot added successfully!';
                                $('#purchase_state').val('1');
                                var $tmp = $('#btnBuyBot');
                                if ($tmp.length)
                                    btnFromBuyToPurchased();
                                break;
                            default:
                                document.getElementById('message').innerText = message == null ? GENERIC_ERROR_STRING : message;
                                $('#purchase_state').val('0');
                                break;
                        }
                    } catch (err) {
                        location.href = "/pages/login.php?redirect=/console/botstore.php?botId=" + botId;
                    }
                },
                complete: function () {
                    document.body.style.cursor = prevCursor;
                },
                error: function () {
                    document.getElementById('btnPayment').disabled = false;
                    document.getElementById('message').innerText = GENERIC_ERROR_STRING;
                }
            });
            break;
        case DRAW_BOTCARDS.BOTSTORE_FLOW.value:
            document.location.href = './newAI.php';
            break;
        default:
    }
}