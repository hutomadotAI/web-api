// on show Modal pass info to tiny purchsed process
$('#buyBot').on('show.bs.modal', function (e) {
    const extraPadding = 170;
    var modalNode = $(e.relatedTarget);
    var curr_bot_id = modalNode.data('botid');
    var curr_bot_name = modalNode.data('name');
    var curr_bot_description = modalNode.data('description');
    var curr_bot_price = modalNode.data('price');
    var curr_bot_license = modalNode.data('license');
    var curr_bot_icon = modalNode.data('icon');
    var curr_bot_flow = modalNode.data('flow');
    var posY = parseFloat(modalNode.offset().top) - extraPadding - window.scrollY;

    document.getElementById('bot_id').value = curr_bot_id;
    document.getElementById('botNamePurchase').innerText = curr_bot_name;
    document.getElementById('botDescriptionPurchase').innerText = curr_bot_description;
    document.getElementById('botPricePurchase').innerText = curr_bot_price;
    document.getElementById('botLicensePurchase').innerText = curr_bot_license;
    document.getElementById('message').innerText = '';
    document.getElementById('btnPayment').disabled = false;
    document.getElementById('btnPayment').setAttribute('data-flow',curr_bot_flow);
    document.getElementById('botIconPurchase').src = curr_bot_icon;

    var dialogNode = document.getElementById('modalDialog');
    dialogNode.style.position = 'relative';
    dialogNode.style.margin = 'auto';
    dialogNode.style.top = posY+'px';


});


