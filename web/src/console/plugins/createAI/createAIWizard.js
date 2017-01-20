function wizardNext() {
    deactiveButtons();

    if(document.newAIbotstoreform.onsubmit) {
        return;
    }

    RecursiveUnbind($('#wrapper'));
    
    var userActivatedBots = getAllBotsLinked();
    var JsonStringActivatedBots = JSON.stringify(userActivatedBots['bots']);
    document.getElementById('userActivedBots').value = JsonStringActivatedBots;
    document.newAIbotstoreform.submit();
}

function backPage(){
    document.newAIbotstoreformGoBack.action = './newAI.php';
    document.newAIbotstoreformGoBack.submit();
}

function deactiveButtons(){
    document.getElementById('btnBack').setAttribute('disabled','disabled');
    document.getElementById('btnNext').setAttribute('disabled','disabled');
    document.getElementById('botSearch').setAttribute('disabled','disabled');
}


function switchCard(botId){
    var node = document.getElementById('card'+botId);
    var btnClassName = 'btn btn-success center-block flat'; //class button to change in switch

    // remove button to  and switch button
    var pos = node.getAttribute('data-pos');
    var wHTML = ('<div class="switch switchOn" data-link="1" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"' + botId+ '","' + pos + '");></div>');
    var targetDiv = node.getElementsByClassName(btnClassName)[0];
    var parent = targetDiv.parentNode;
    parent.setAttribute('data-linked','1');
    parent.innerHTML = wHTML;

    // add active border to card
    node.children[0].children[0].classList.add("borderActive");
}


function getAllBotsLinked(){
    var userActivedBots = {};
    var i, $list = $('.bot-linked');

    userActivedBots.bots = new Array();

    for (var i = 0; i<$list.length; i++) {
        var temp_item = $list.eq(i);

        userActivedBots.bots.push({
            "botId" : temp_item.attr('data-botid'),
            "active"  : temp_item.attr('data-linked'),
        });
    }
    return userActivedBots;
}