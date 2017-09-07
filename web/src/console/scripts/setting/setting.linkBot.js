function showAddSkills(str,  bots, botSubSet ) {
    str = str.toLowerCase();
    var botArray = [];
    var index = 0;
    for (var x in bots) {
        if ((str !== " ") && ( (str.length === 0) || (bot['name'].toLowerCase()).indexOf(str) !== -1 )) {
            var bot = JSON.parse(bots[x]);
            var botId = bot['botId'];
            var botItem = {
                index: index++,
                botId: botId,
                botName: bot['name'],
                category: encodeURIComponent(bot['category']),
                imagePath: bot['imagePath']
            };
            if ($.inArray(botId, botSubSet) !== -1) {
                botItem.isLinked = true;
            }
            botArray.push(botItem);
        }
    }

    $.get('templates/botcard_link.mustache', function(template) {
        $('#botsSearch').html(Mustache.render(template, {bots: botArray}));
    });
}

function activeRightMenu(response) {
    if (response === '1') {
        document.getElementById('page_general').classList.remove('active');
        document.getElementById('page_aiskill').classList.add('active');
        document.getElementById('tab_general').className = '';
        document.getElementById('tab_aiskill').className = ('active');
    }
}