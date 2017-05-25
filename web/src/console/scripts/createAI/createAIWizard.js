function wizardNext() {
    deactiveButtons();

    if (document.newAIbotstoreform.onsubmit) {
        return;
    }

    RecursiveUnbind($('#wrapper'));

    var userActivatedBots = getAllBotsLinked();
    var JsonStringActivatedBots = JSON.stringify(userActivatedBots['bots']);
    document.getElementById('userActivedBots').value = JsonStringActivatedBots;
    document.newAIbotstoreform.submit();
}

function backPage() {
    document.newAIbotstoreformGoBack.action = './newAI.php';
    document.newAIbotstoreformGoBack.submit();
}

function deactiveButtons() {
    document.getElementById('btnBack').setAttribute('disabled', 'disabled');
    document.getElementById('btnNext').setAttribute('disabled', 'disabled');
}

function getAllBotsLinked() {
    var userActivedBots = {};
    var i, $list = $("[data-linked=\"1\"]");

    userActivedBots.bots = new Array();

    for (var i = 0; i < $list.length; i++) {
        var temp_item = $list.eq(i);

        userActivedBots.bots.push({
            "botId": temp_item.attr('data-botid'),
            "active": temp_item.attr('data-linked'),
        });
    }
    return userActivedBots;
}

$('#buyBot').on('hide.bs.modal', function (e) {
    var purchase_state = document.getElementById('purchase_state').value;
    if (purchase_state ==='true')
        switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value);
});

