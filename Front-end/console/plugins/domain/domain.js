function wizardNext() {
    deactiveButtons();

    if(document.domainsNewAIform.onsubmit) {
        return;
    }

    RecursiveUnbind($('#wrapper'));
    var JsonStringActiveDomains = JSON.stringify(userActived);
    //alert(JsonStringActiveDomains);
    document.getElementById('userActivedDomains').value = JsonStringActiveDomains;
    document.domainsNewAIform.submit();
}

function backPage(){
    document.domainsNewAIformGoBack.action = './newAI.php';
    document.domainsNewAIformGoBack.submit();
}

function showDomains(str,option,category){
    var wHTML = "";

    for (var x in domains) {
        var boxid = domains[x].aiid;

        var fake_category = 'All';
        if ( (str!=" ") && ( (str.length==0) || (domains[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
            var key = domains[x].aiid;

            if ( category =='' || category==fake_category) {
                if (userActived[key] === false)
                    wHTML += ('<div class="col-lg-2 col-md-3 col-sm-4 col-xs-4"><div class="box box-solid box-default-fixed flat" id="' + boxid + '">');
                else
                    wHTML += ('<div class="col-lg-2 col-md-3 col-sm-4 col-xs-4"><div class="box box-solid box-default-fixed flat borderActive" id="' + boxid + '">');

                if (option == 0)
                    wHTML += ('<div class="info-circle-icon ' + domains[x].widgetColor + '" style="margin-top: 40px;" onMouseOver="this.style.cursor=\'pointer\'"><i class="' + domains[x].iconPath + '"></i></div>');
                else
                    wHTML += ('<div class="info-circle-icon ' + domains[x].widgetColor + '" style="margin-top: 40px;" onMouseOver="this.style.cursor=\'pointer\'" onClick=openSingleBot(this,"' + domains[x].aiid + '"); ><i class="' + domains[x].iconPath + '"></i></div>');

                wHTML += ('<h4 class="text-center text-mute unselectable">' + domains[x].name + '</h4>');
                wHTML += ('<h5 class="text-center text-gray unselectable" style="padding-left:5px;padding-right:5px;">' + domains[x].description + '</h5>');

                wHTML += addHtmlStarRating(userActived[key], boxid, domains[x].rating);
                wHTML += ('<div class="box-footer-flatdown flat"><h5 class="text-center text-light-blue unselectable" onMouseOver="this.style.cursor=\'pointer\'" onClick=openSingleBot(this,"' + domains[x].aiid + '"); >info and settings</h5>');

                if (userActived[key] === false)
                    wHTML += ('<div class="switch" data-rnn="0" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"' + key + '");></div>');
                else
                    wHTML += ('<div class="switch switchOn" data-rnn="1" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"' + key + '");></div>');
                wHTML += ('</div>');
                wHTML += ('</div></div>');
            }
        }
    }
    newNode.innerHTML = wHTML;
    document.getElementById('domsearch').appendChild(newNode);
}

function switchClick(node,key){
    var boxid = key;
    $(node).toggleClass('switchOn');
    if( $(node).attr('data-rnn') == '0') {
        $(node).attr('data-rnn', 1);
        $("#"+boxid).addClass("borderActive");
        userActived[key] = true;
    }
    else {
        $(node).attr('data-rnn', 0);
        userActived[key] = false;
        $("#"+boxid).removeClass("borderActive");
    }
}

function deactiveButtons(){
    document.getElementById('btnBack').setAttribute('disabled','disabled');
    document.getElementById('btnNext').setAttribute('disabled','disabled');
    document.getElementById('domsearch').setAttribute('disabled','disabled');
}

function addHtmlStarRating(actived,boxid,rating){
    var wHTML='';

    wHTML += ('<div class="box-footer-stars flat">');
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

function openSingleBot(elem,aiid){
    elem.setAttribute('onClick','');

    var form = document.createElement("form");
    document.body.appendChild(form);
    form.method = "POST";
    form.action = "./singlebotstore.php";
    var element = document.createElement("INPUT");
    element.name="aiid"
    element.value = aiid;
    element.type = 'hidden'
    form.appendChild(element);
    form.submit();
}

// Pass values to Modal on show dialog modal
$('#boxBotStoreInfo').on('show.bs.modal', function(e) {
    var curr_bot_name = $(e.relatedTarget).data('name').toUpperCase();
    var curr_bot_description = $(e.relatedTarget).data('description');

    var curr_bot_icon ='fa fa-user';
    var curr_bot_color = 'gray';
    // TODO need to have from getAI specific usecase
    var curr_bot_usescase = 'This AI is used for la bla bla bla bla bla';
    // TODO need to have from getAI more details
    var curr_bot_details= 'bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ' +
        'bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla';
    /*
    if ( $(e.relatedTarget).data('iconPath') != '' )
        curr_domain_icon = $(e.relatedTarget).data('iconPath');
    if ( $(e.relatedTarget).data('widgetColor') !='')
        curr_domain_color = $(e.relatedTarget).data('widgetColor');
        */
    $(e.currentTarget).find('span').text(curr_bot_name);
    $(e.currentTarget).find('h3').text(curr_bot_description);
    $(e.currentTarget).find('dd').text(curr_bot_details);
    $(e.currentTarget).find('df').text(curr_bot_usescase);
    
    $(e.currentTarget).find('i').attr('class', curr_bot_icon +' text-md text-gray');
    $(e.currentTarget).find('.modal-header').attr('class', 'modal-header ' + curr_bot_color);
});
