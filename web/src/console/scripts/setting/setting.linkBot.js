function showAddSkills(str,  bots, botSubSet ) {
    var wHTML = "";
    str = str.toLowerCase();
    for (var x in bots) {
        var bot = JSON.parse(bots[x]);
        var botId = bot['botId'];
        var botName = bot['name'];
        var category = bot['category'];

        if ((str !== " ") && ( (str.length === 0) || (bot['name'].toLowerCase()).indexOf(str) !== -1 )) {
            var isActiveBot = ($.inArray(botId, botSubSet) !== -1);
            var openBotDetails = 'onClick="location.href=\'botstore.php?botId=' + botId + '&category=' + encodeURIComponent(category) + '\'");';

            wHTML += ('<span id="card' + botId + '" data-pos="' + x + '">');
            wHTML += ('<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1' + ((isActiveBot) ? ' borderActive">' : '">'));
            wHTML += ('<img class="card-icon unselectable" src="' + bot['imagePath'] + '"' + openBotDetails +'>');
            wHTML += '<div class="card-title unselectable no-shadow"' + openBotDetails + '>';
            wHTML += botName;
            wHTML += '</div>';
            wHTML += '<div class="card-author unselectable no-shadow">';
            wHTML += '</div>';
            wHTML += ('<div class="card-footer flat unselectable">');
            wHTML += ('<div class="row no-margin">');
            wHTML += ('<div class="pull-left">');

            // TODO when API in ready we can add this infos
            //wHTML += ('<i class="fa fa-star card-star"></i>');
            //wHTML += ('<span class="card-users text-left">'+ bot['activations']+'</span>');
            wHTML += ('</div>');

            if (isActiveBot) {
                wHTML += ('<span class="card-linked" data-botid = "' + botId + '" data-linked="true">');
                wHTML += ('<div class="switch switchOn" id="btnSwitch' + botId + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,' + DRAW_BOTCARDS.ADD_SKILL_FLOW.value + ',"' + botId + '"); data-link="true"></div>');
            } else {
                wHTML += ('<span class="card-linked" data-botid = "' + botId + '" data-linked="false">');
                wHTML += ('<div class="switch" id="btnSwitch' + botId + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,' + DRAW_BOTCARDS.ADD_SKILL_FLOW.value + ',"' + botId + '"); data-link="false"></div>');
            }

            wHTML += ('</span>');
            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</span>');
        }
    }
    newNode.innerHTML = wHTML;
    document.getElementById('botsSearch').appendChild(newNode);
}

function activeRightMenu(response) {
    if (response === '1') {
        document.getElementById('page_general').classList.remove('active');
        document.getElementById('page_aiskill').classList.add('active');
        document.getElementById('tab_general').className = '';
        document.getElementById('tab_aiskill').className = ('active');
    }
}