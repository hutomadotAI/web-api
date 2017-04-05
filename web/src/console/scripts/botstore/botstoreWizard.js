function infoForBotstore(title, purchased) {
    var v = [];

    switch (title) {
        case 'home' :
            v['menu_title'] = title;
            v['menu_level'] = 0;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            if (purchased == 'true')
                btnFromBuyToPurchased();

            document.getElementById('btnBuyBotBack').setAttribute('href', './NewAIBotstore.php');
            document.getElementById('bthBackToBotstore').innerText = 'Go back';
            document.getElementById('bthBackToBotstore').setAttribute('href', './NewAIBotstore.php');

            break;
        case 'settings' :
            v['menu_title'] = title;
            v['menu_level'] = 1;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            document.getElementById('btnBuyBotBack').setAttribute('href', './settingsAI.php?botstore=1');


            btnFromBuyToPurchased();

            break;
        case 'botstore' :

            v['menu_title'] = 'botstore';
            v['menu_level'] = 2;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;

            if (purchased == 'true')
                btnFromBuyToPurchased();

            document.getElementById('btnBuyBotBack').setAttribute('href', './botstore.php');
    }
    return v;
}