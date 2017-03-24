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
    ADD_SKILL_FLOW: {value: 2}
};

function showBots(str, option, bots, botSubSet ) {
    var wHTML = "";
    str = str.toLowerCase();
    for (var x in bots) {
        var bot = JSON.parse(bots[x]);
        if ((str != " ") && ( (str.length == 0) || (bot['name'].toLowerCase()).indexOf(str) != -1 )) {

            var openBotDetails = 'onClick=openSingleBot(this,' + option + ',"' + bot['botId'] + '",' + ($.inArray(bot['botId'], botSubSet) != -1) + ');';
            wHTML += ('<span id="card' + bot['botId'] + '" data-pos="' + x + '">');

            if ( (option == DRAW_BOTCARDS.ADD_SKILL_FLOW.value) && ($.inArray(bot['botId'], botSubSet) != -1) )
                wHTML += ('<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1 borderActive">');
            else
                wHTML += ('<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1">');

            wHTML += ('<img class="card-icon unselectable" src="' + bot['imagePath'] + '"' + openBotDetails +'>');

            wHTML += ('<div class="card-title unselectable"' + openBotDetails +'>');
            wHTML += ('<p>' + bot['name'] + '</p>');
            wHTML += ('</div>');

            wHTML += ('<div class="card-author unselectable">');
            // TODO when API in ready we can add this infos
            //wHTML += ('<p>by ' + bot['name'] + '</p>');
            wHTML += ('</div>');

            wHTML += ('<div class="card-footer flat unselectable">');
            wHTML += ('<div class="row no-margin">');

            wHTML += ('<div class="pull-left">');
            // TODO when API in ready we can add this infos
            //wHTML += ('<i class="fa fa-star card-star"></i>');
            //wHTML += ('<span class="card-users text-left">'+ bot['activations']+'</span>');
            wHTML += ('</div>');

            var dataBuyBot = 'id="btnBuyBot' + bot['botId']
                + '" data-toggle="modal" data-target="#buyBot" data-botid="' + bot['botId'] + '" data-name="' + bot['name']
                + '" data-description="' + bot['description']
                + '" data-icon="' + bot['imagePath'] + '" data-price="' + bot['price'] + '"';
            switch (option) {
                case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:  // botstore showed during creation AI wizard
                    wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="">');
                    if ($.inArray(bot['botId'], botSubSet) != -1) {
                        wHTML += ('<div class="switch" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,"' + bot['botId'] + '"); data-link="0"></div>');
                    }
                    else {
                        wHTML += ('<div class="card-price pull-right" ' + dataBuyBot + '>');
                        wHTML += (bot['price']+ ' &#8364');
                        wHTML += ('</div>');
                    }
                    break;
                case DRAW_BOTCARDS.BOTSTORE_FLOW.value:  // botstore showed in BOTSTORE
                    wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="">');
                    if ($.inArray(bot['botId'], botSubSet) != -1) {
                        wHTML += ('<div class="card-purchased pull-right">');
                        wHTML += ('purchased');
                        wHTML += ('</div>');
                    }
                    else {
                        wHTML += ('<div class="card-price pull-right" ' + dataBuyBot + '>');
                        wHTML += (bot['price']+ ' &#8364');
                        wHTML += ('</div>');
                    }
                    break;
                default:
                    if ($.inArray(bot['botId'], botSubSet) != -1) {
                        wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="1">');
                        wHTML += ('<div class="switch switchOn" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,"' + bot['botId'] + '"); data-link="1"></div>');
                    } else {
                        wHTML += ('<span class="card-linked" data-botid = "' + bot['botId'] + '" data-linked="0">');
                        wHTML += ('<div class="switch" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=toggleAddBotSkill(this,"' + bot['botId'] + '"); data-link="0"></div>');
                    }

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

function openSingleBot(elem, option, botId, purchased) {
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

    element = document.createElement("INPUT");
    element.name = "purchased";
    element.value = purchased;
    element.type = 'hidden';
    form.appendChild(element);

    element = document.createElement("INPUT");
    element.name = "menu_title";
    switch(option){
        case DRAW_BOTCARDS.CREATE_NEW_BOT_FLOW.value:
            element.value = 'home';
            break;
        case DRAW_BOTCARDS.BOTSTORE_FLOW.value:
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

function toggleAddBotSkill(node, botId) {
    var MAX_LINKED_BOTS = 5;
    var parent = node.parentNode;

    var listActive = document.getElementById('botsSearch').children[0].getElementsByClassName('borderActive');

    if (!node.classList.contains('switchOn') && listActive.length >= MAX_LINKED_BOTS) {
        alert("You can only combine up to " + MAX_LINKED_BOTS + " bots.");
        return;
    }

    $(node).toggleClass('switchOn');
    if ($(node).attr('data-link') == '0') {
        $(node).attr('data-link', 1);
        parent.setAttribute('data-linked', '1');
        document.getElementById('card' + botId).children[0].classList.add("borderActive");
    }
    else {
        $(node).attr('data-link', 0);
        parent.setAttribute('data-linked', '0');
        document.getElementById('card' + botId).children[0].classList.remove("borderActive");
    }
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