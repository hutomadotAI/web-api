// on show Modal pass info to tiny purchsed process
$('#buyBot').on('show.bs.modal', function (e) {
    var curr_bot_id = $(e.relatedTarget).data('botid');
    var curr_bot_name = $(e.relatedTarget).data('name');
    var curr_bot_description = $(e.relatedTarget).data('description');
    var curr_bot_price = $(e.relatedTarget).data('price');
    var curr_bot_license = $(e.relatedTarget).data('license');
    var curr_bot_icon = $(e.relatedTarget).data('icon');
    var curr_bot_flow = $(e.relatedTarget).data('flow');

    document.getElementById('bot_id').value = curr_bot_id;
    document.getElementById('botNamePurchase').innerText = curr_bot_name;
    document.getElementById('botDescriptionPurchase').innerText = curr_bot_description;
    document.getElementById('botPricePurchase').innerText = curr_bot_price;
    document.getElementById('botLicensePurchase').innerText = curr_bot_license;
    document.getElementById('message').innerText = '';
    document.getElementById('btnPayment').disabled = false;
    document.getElementById('btnPayment').setAttribute('data-flow',curr_bot_flow);
    document.getElementById('botIconPurchase').src = curr_bot_icon;
});