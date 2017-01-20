document.getElementById("btnPayment").addEventListener("click", purchaseBot);

function purchaseBot(){
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    document.getElementById('btnPayment').disabled = true;
    var botId = document.getElementById('bot_id').value;
    document.getElementById('message').innerText = 'Sending request...';
    $.ajax({
        url: './dynamic/purchaseBot.php',
        data: { botId: botId } ,
        type: 'POST',
        success: function (response) {

            var statusCode = JSON.parse(response);
            switch(statusCode['status']['code']){
                case 200:
                    document.getElementById('message').innerText = 'Skill succesfully added!';
                    $('#purchase_state').val('1');
                    var $tmp = $('#btnBuyBot');;
                    if ( $tmp.length)
                        btnFromBuyToPurchased();
                    break;
                case 400:
                    document.getElementById('message').innerText = 'You have already bought.';
                    break;
                case 404:
                    document.getElementById('message').innerText = 'Purchase Denied!';
                    $('#purchase_state').val('0');
                    break;
                case 500:
                    document.getElementById('message').innerText = 'Purchase Denied!';
                    $('#purchase_state').val('0');
                    break;
            }
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            document.getElementById('btnPayment').disabled = false;
            document.getElementById('message').innerText = 'Purchase Denied!';
        }
    });
}
