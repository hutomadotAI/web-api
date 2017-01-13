function switchCard(botId){
    var node = document.getElementById('card'+botId);
    var btnClassName = 'btn btn-success center-block flat'
    var pos = node.getAttribute('data-pos');
    var targetDiv = node.getElementsByClassName(btnClassName)[0];

    targetDiv.classList.remove("btn-success");
    targetDiv.classList.add("btn-primary");

    targetDiv.setAttribute('data-toggle','');
    targetDiv.setAttribute('data-target','');
    targetDiv.innerHTML = ('<b>Bot purchased </b><span class="fa fa-check-circle-o"></span>');

    node.children[0].children[0].classList.add("borderActive");
}


function codeHTMLstars(rating){
    var wHTML='';
    for (var i=5; i>0; i--) {
        if ( i== Math.round(rating)) {
            wHTML += '<input class="star-rating__input" id="star--rating-' + i + '" type="radio" name="rating" value="' + i + '" checked="checked" disabled="disabled">';
            wHTML += '<label class="star-rating__ico fa fa-star-o fa-lg" for="star--rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>';
        }
        else {
            wHTML += '<input class="star-rating__input" id="star--rating-' + i + '" type="radio" name="rating" value="' + i + '" disabled="disabled">';
            wHTML += '<label class="star-rating__ico fa fa-star-o fa-lg" for="star--rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>';
        }
    }
    return wHTML;
}


function populateBotFields(bot){
    var json = JSON.parse(bot);
    document.getElementById('botTitle').innerText = json['name'];
    document.getElementById('botBadge').innerText = json['badge'];
    document.getElementById('botDescription').innerText = json['description'];
    document.getElementById('botUsers').innerText = json['users'];
    document.getElementById('botRating').innerHTML = codeHTMLstars(json['rating']);
    document.getElementById('botMessage').innerText = json['alertMessage'];
    document.getElementById('botLicense').innerText = json['licenseType'];
    document.getElementById('botPrice').innerText = json['price'];
    document.getElementById('botLongDescription').innerText = json['longDescription'];
    document.getElementById('botSample').innerText = json['sample'];
    document.getElementById('botLastUpdate').innerText = json['lastUpdate'];
    document.getElementById('botCategory').innerText = json['category'];
    document.getElementById('botVersion').innerText = json['version'];
    document.getElementById('botClassification').innerText = json['classification'];
    document.getElementById('botCompany').innerText = 'hu:toma'; //json['company'];
    document.getElementById('botActivations').innerText = json['activations'];
    document.getElementById('botReport').setAttribute('href',json['report']);
    document.getElementById('botPrivacyPolicy').setAttribute('href',json['privacyPolicy']);
    document.getElementById('botWebsite').setAttribute('href',json['website']);
    document.getElementById('botDeveloper').innerText = 'hutoma';
    document.getElementById('botEmail').innerText = 'support@hutoma.com';
    document.getElementById('botAddress').innerText = 'Carrer del Consell de Cent, 341';
    document.getElementById('botPostCode').innerText = '08007';
    document.getElementById('botCity').innerText = 'Barcelona';
    document.getElementById('botCountry').innerText = 'Spain';


    /*
     document.getElementById('bot_aiid').value = json['aiid'];
     document.getElementById('bot_videoLink').value = json['videoLink'];
     */

    document.getElementById('botNamePurchase').innerText = json['name'];
    document.getElementById('botDescriptionPurchase').innerText = json['description'];
    document.getElementById('botPricePurchase').innerText = json['price'];
    document.getElementById('bot_id').value = json['botId'];
}

function infoForBotstore(title,purchased){
    var v=[];
    switch(title){
        case 'settings' :
            v['menu_title']= title;
            v['menu_level']= 1;
            v['menu_block']= false;
            v['menu_active']= false;
            v['menu_deep']= 0;

            document.getElementById('btnBuyBotBack').setAttribute('href','./settingsAI.php?botstore=1');
            //document.getElementById('btnBuyBotBack').innerText = 'Back to Ai Skill';

            var wHTML = '';
            var nodeBtn = document.getElementById('btnBuyBot');
            wHTML += ('<b>Bot purchased </b>');
            wHTML += ('<span class="fa fa-check-circle-o"></span>');
            nodeBtn.setAttribute('data-toggle','');
            nodeBtn.setAttribute('data-target','');
            nodeBtn.innerHTML = wHTML;
            nodeBtn.className = 'btn btn-primary pull-right flat';

            break;
        case 'botstore' :

            v['menu_title']= 'botstore';
            v['menu_level']= 2;
            v['menu_block']= false;
            v['menu_active']= false;
            v['menu_deep']= 0;
            
            if(purchased=='true'){
                var wHTML = '';
                var nodeBtn = document.getElementById('btnBuyBot');
                wHTML += ('<b>Bot purchased </b>');
                wHTML += ('<span class="fa fa-check-circle-o"></span>');
                nodeBtn.setAttribute('data-toggle','');
                nodeBtn.setAttribute('data-target','');
                nodeBtn.innerHTML = wHTML;
                nodeBtn.className = 'btn btn-primary pull-right flat';
            }

            document.getElementById('btnBuyBotBack').setAttribute('href','./botstore.php');
            //document.getElementById('btnBuyBotBack').innerText = 'Back to Botstore';
    }
   return v;
}

