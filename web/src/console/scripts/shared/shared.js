var ALERT =
{
    BASIC: {value: 0},
    WARNING: {value: 1},
    DANGER: {value: 2},
    SUCCESS: {value: 3},
    INFO: {value: 4},
    PRIMARY: {value: 5}
};

var API_AI_STATE =
{
    UNDEFINED: {value: 'ai_undefined'},
    QUEUED: {value: 'ai_training_queued'},
    READY_TO_TRAIN: {value: 'ai_ready_to_train'},
    TRAINING: {value: 'ai_training'},
    STOPPED: {value: 'ai_training_stopped'},
    COMPLETED: {value: 'ai_training_complete'},
    ERROR: {value: 'ai_error'}
};

var UI_STATE =
{
    ERROR: {value: -1},
    NOTHING: {value: 0},
    FILE_UPLOADED: {value: 1},
    READY_TO_TRAIN: {value: 2},
    PHASE1_INIT: {value: 3},
    PHASE1_RUN: {value: 4},
    PHASE2_QUEUE: {value: 5},
    PHASE2_INIT: {value: 6},
    PHASE2_RUN: {value: 7},
    STOPPED: {value: 8},
    COMPLETED: {value: 10},
    LISTENING_MODE: {value: 999}
};

var UI_TRAINING_STATE =
{
    PHASE1_INIT: {value: 100},
    PHASE1_RUN: {value: 101},
    PHASE1_END: {value: 102},
    PHASE2_INIT: {value: 200},
    PHASE2_RUN: {value: 201}
};

var INTENT_ACTION =
{
    DELETE_INTENT: {value: false},
    SAVE_INTENT: {value: true}
};

var DRAW_BOTCARDS =
{
    CREATE_NEW_BOT_FLOW: {value: 0},
    BOTSTORE_FLOW: {value: 1},
    BOTSTORE_WITH_BOT_FLOW: {value: 2},
    ADD_SKILL_FLOW: {value: 3}
};

var BOT_ICON = {
    PATH: {value: 'dist/img/boticon/'},
    DEFAULT_IMAGE: {value: 'dist/img/default_bot.jpg'}
};

var URLS = {
    HUTOMA_CONSOLE : 'https://console.hutoma.com'
};

function switchCard(botId,optionFlow) {
    var node = document.getElementById('card' + botId);
    var btnClassName = 'card-price pull-right';
    var pos = node.getAttribute('data-pos');
    var targetDiv = node.getElementsByClassName(btnClassName)[0];
    switch (optionFlow) {
        case DRAW_BOTCARDS.BOTSTORE_FLOW.value:
            targetDiv.classList.remove('card-price');
            targetDiv.classList.add('card-purchased');
            targetDiv.setAttribute('data-toggle', '');
            targetDiv.setAttribute('data-target', '');
            targetDiv.innerHTML = ('purchased');
            document.getElementById('cardTestBotLink' + botId).style.display = 'block';
            break;
        case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:
            var wHTML = ('<div class="switch" data-link="0" id="btnSwitch' + botId + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,' + optionFlow + ',"' + botId + '");></div>');
            var parent = targetDiv.parentNode;
            parent.setAttribute('data-linked', '0');
            parent.innerHTML = wHTML;
            break;
        default:
            console.log("Option flow has a wrong value")
    }
}

function openSingleBot(elem, option, botId, category) {
    elem.setAttribute('onClick', '');

    var form = document.createElement("form");
    document.body.appendChild(form);
    form.method = "POST";
    form.action = "./dynamic/sessionBotMenu.php";

    var element = document.createElement("INPUT");
    element.name = "botId";
    element.value = botId;
    element.type = 'hidden';
    form.appendChild(element);

    if (category!==undefined) {
        element = document.createElement("INPUT");
        element.name = "category";
        element.value = category;
        element.type = 'hidden';
        form.appendChild(element);
    }

    element = document.createElement("INPUT");
    element.name = "menu_title";
    switch(option){
        case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:
            element.value = 'home';
            break;
        case DRAW_BOTCARDS.BOTSTORE_FLOW.value:
            element.value = 'botstore';
            break;
        case DRAW_BOTCARDS.BOTSTORE_WITH_BOT_FLOW.value:
            element.value = 'botstore';
            break;
        case DRAW_BOTCARDS.ADD_SKILL_FLOW.value:
            element.value = 'settings';
            break;
        default:
    }
    element.type = 'hidden';
    form.appendChild(element);
    form.submit();
}

function RecursiveUnbind($jElement) {
    // remove this element's and all of its children's click events
    $jElement.unbind();
    $jElement.removeAttr('onclick');
    $jElement.removeAttr('href');
    $jElement.removeAttr('type');
    $jElement.children().each(function () {
        RecursiveUnbind($(this));
    });
}

function toggleAddBotSkill(node, optionFlow, botId) {
    var MAX_LINKED_BOTS = 5;
    var parent = node.parentNode;
    var activatedBots = 0;

    switch(optionFlow){
        case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:
            activatedBots = document.getElementById('botsCarousels').getElementsByClassName('borderActive').length;
            break;
        case DRAW_BOTCARDS.ADD_SKILL_FLOW.value:
            activatedBots = document.getElementById('botsSearch').children[0].getElementsByClassName('borderActive').length;
            break;
        default:
    }

    if (!node.classList.contains('switchOn') && parseInt(activatedBots) >= MAX_LINKED_BOTS) {
        alert("You can only combine up to " + MAX_LINKED_BOTS + " bots.");
        return;
    }

    $(node).toggleClass('switchOn');
    var botcard = document.getElementById('card' + botId).children[0];

    if ($(node).attr('data-link') == '0') {
        $(node).attr('data-link', 1);
        parent.setAttribute('data-linked', '1');
        botcard.classList.add("borderActive");
    }
    else {
        $(node).attr('data-link', 0);
        parent.setAttribute('data-linked', '0');
        botcard.classList.remove("borderActive");
    }
}

function btnFromBuyToPurchased() {
    var wHTML = '';
    var nodeBtn = document.getElementById('btnBuyBot');
    wHTML += ('<b>Bot purchased </b>');
    wHTML += ('<span class="fa fa-check-circle-o"></span>');
    nodeBtn.setAttribute('data-toggle', '');
    nodeBtn.setAttribute('data-target', '');
    nodeBtn.innerHTML = wHTML;
    nodeBtn.setAttribute('onClick','');
    nodeBtn.className = 'btn btn-primary pull-right flat';
}

function buildCategoryURIparameter(category){
    return '?category='+ adjustURIEscapingCategoryValue(category);
}

function adjustURIEscapingCategoryValue(value){
    return value.replace('&', '%26').split(' ').join('%20');
}

function removeSpecialCharacters(str){
    return str.replace(/[&\/\\#,+()$~%.'":*?<>{}\s+]/g, '');
}

function htmlEncode(value){
    return $('<div/>').text(value).html();
}

$(document).ready(function () {
    var is_chrome = navigator.userAgent.indexOf('Chrome') > -1;
    var is_safari = navigator.userAgent.indexOf("Safari") > -1;
    var is_mac = (navigator.userAgent.indexOf('Mac OS') != -1);
    var is_windows = !is_mac;
    if (is_chrome && is_safari) {
        is_safari = false;
    }
    if (is_safari || is_windows) {
        $('body').css(
            "font-family", "'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif"
        );
    }
});