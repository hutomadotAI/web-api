function showBots(str,option){
    var wHTML = "";
    for (var x in purchasedBots) {
        var bot = JSON.parse(purchasedBots[x]);

        if ( (str!=" ") && ( (str.length==0) || (bot['name'].toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {

            wHTML += ('<span id="card' + bot['botId'] + '" data-pos="'+x+'">');
            wHTML += ('<div class="col-lg-2 col-md-3 col-sm-4 col-xs-4">');
            if($.inArray(bot['botId'], linkedBots)!=-1)
                wHTML += ('<div class="box box-solid box-default-fixed flat borderActive" id="' + bot['aiid'] + '">');
            else
                wHTML += ('<div class="box box-solid box-default-fixed flat" id="' + bot['aiid'] + '">');
            wHTML += ('<div class="info-circle-icon bg-blue-gradient" style="margin-top: 40px;" onMouseOver="this.style.cursor=\'pointer\'" onClick=openSingleBot(this,"' + bot['botId'] + '"); ><i class="fa fa-question"></i></div>');
            wHTML += ('<h4 class="text-center text-mute unselectable">' + bot['name'] + '</h4>');
            wHTML += ('<h5 class="text-center text-gray unselectable" style="padding-left:5px;padding-right:5px;">' + bot['description'] + '</h5>');
            wHTML += ('<div class="box-footer-flatdown flat">');
            wHTML += addHtmlStarRating(0, bot['aiid'], bot['rating']);
            wHTML += ('<h5 class="text-center text-light-blue unselectable" onMouseOver="this.style.cursor=\'pointer\'" onClick=openSingleBot(this,"' + bot['botId'] + '"); >info and details</h5>');
            if ($.inArray(bot['botId'], linkedBots) != -1) {
                wHTML += ('<span class="bot-linked" data-botid = "' + bot['botId'] + '" data-linked="1">');
                wHTML += ('<div class="switch switchOn" data-link="1" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=switchClick(this,"' + bot['botId'] + '","' + x + '");></div>');
            }else {
                wHTML += ('<span class="bot-linked" data-botid = "' + bot['botId'] + '" data-linked="0">');
                wHTML += ('<div class="switch"  data-link="0" id="btnSwitch' + bot['botId'] + '" style="margin-top:10px;" onclick=switchClick(this,"' + bot['botId'] + '");></div>');
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


function addHtmlStarRating(actived,boxid,rating){
    var wHTML='';

    wHTML += ('<div class="flat">');
    wHTML += ('<div class="star-rating text-center">');
    wHTML += ('<div class="star-rating__wrap">');

    if ( actived ) {
        for (var i=5; i>0; i--) {
            if (i==Math.round(rating))
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" checked="checked">');
            else
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '">');
            wHTML += ('<label class="star-rating__ico fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="'+i+' out of '+i+' stars"></label>');
        }
    }else {
        // TODO if input is disable need add to input disabled="disabled" and in label icon __disabled - now the code is same
        for (var i = 5; i > 0; i--) {
            if (i==Math.round(rating))
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" checked="checked">');
            else
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '">');
            wHTML += ('<label class="star-rating__ico fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>');
        }
    }
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');
    return wHTML;
}


function switchClick(node,botId,pos){
    var parent = node.parentNode;

    $(node).toggleClass('switchOn');
    if( $(node).attr('data-link') == '0') {

        $(node).attr('data-link', 1);
        parent.setAttribute('data-linked','1');
        document.getElementById('card'+botId).children[0].children[0].classList.add("borderActive");
    }
    else {
        $(node).attr('data-link', 0);
        parent.setAttribute('data-linked','0');
        document.getElementById('card'+botId).children[0].children[0].classList.remove("borderActive");
    }
}


function openSingleBot(elem,botId){
    elem.setAttribute('onClick','');

    var form = document.createElement("form");
    document.body.appendChild(form);
    form.method = "POST";
    form.action = "./singlebotstore.php";

    var element = document.createElement("INPUT");
    element.name="botId";
    element.value = botId;
    element.type = 'hidden';
    form.appendChild(element);

    var element = document.createElement("INPUT");
    element.name="menu_title";
    element.value = 'settings';
    element.type = 'hidden';
    
    form.appendChild(element);

    form.submit();
}


function activeRightMenu(response){
    if (response== '1') {
        document.getElementById('page_general').classList.remove('active');
        document.getElementById('page_aiskill').classList.add('active');
        document.getElementById('tab_general').className = '';
        document.getElementById('tab_aiskill').className = ('active');
    }
}