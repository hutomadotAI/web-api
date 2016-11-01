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

function showDomains(str,size){
    var wHTML = "";

    for (var x in domains) {
        var boxid = domains[x].aiid;
        if ( (str!=" ") && ( (str.length==0) || (domains[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
            if(size==0){
                // slim box design
               // if ( domains[x].published == '1' ){
                    /* available equal to ZERO
                    wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow" id="'+boxid+'"><p></p>');
                    wHTML += ('<div class="col-xs-2">');
                    wHTML += ('<div class="info-circle-icon-small text-ultragray"><i class="'+domains[x].icon+'"></i></div>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-7">');
                    wHTML += ('<h4 class="text-center text-center text-gray" style="text-align: left;">&nbsp;'+domains[x].name+'</h4>');
                    wHTML += ('<h5 class="text-center text-center text-gray" style="text-align: left;">&nbsp;'+domains[x].description+'</h5>');
                    wHTML += ('<h5 class="text-center text-ultralight-blue" style="text-align: left;">&nbsp;COMING SOON</h5>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-3">');
                    wHTML += ('<div class="switchOff" style="margin-top:33px;"></div>');
                    wHTML += ('</div>');
                    wHTML += ('</div></div>');
                    */

                    var key = domains[x].aiid;

                    if ( userActived[key] === false )
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow" id="'+boxid+'"><p></p>');
                    else
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow borderActive" id="'+boxid+'"><p></p>');
                    wHTML += ('<div class="col-xs-2">');
                    wHTML += ('<div class="info-circle-icon-small '+domains[x].widgetColor+'"><i class="'+domains[x].iconPath+'"></i></div>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-7">');
                    wHTML += ('<b><h4 class="text-center text-white" style="text-align: left;">&nbsp;'+domains[x].name+'</h4></b>');
                    wHTML += ('<h5 class="text-center text-white" style="text-align: left;">&nbsp;'+domains[x].description+'</h5>');
                    wHTML += ('<a data-toggle="modal" ' +
                    'data-target="#boxDomainInfo" ' +
                    'data-id="'+domains[x].aiid+'" ' +
                    'data-name="'+domains[x].name+'" ' +
                    'data-icon="'+domains[x].iconPath+'" ' +
                    'data-color="'+domains[x].widgetColor+'" ' +
                    'style="cursor: pointer;">');
                    wHTML += ('<h5 class="text-center text-light-blue" style="text-align: left;">&nbsp;info and settings</h5>');
                    wHTML += ('</a>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-3">');

                    if ( userActived[key] === false )
                        wHTML += ('<div class="switch" data-rnn="0" style="margin-top:33px;" onclick=switchClick(this,"'+key+'");></div>');
                    else
                        wHTML += ('<div class="switch switchOn" data-rnn="1" style="margin-top:33px;" onclick=switchClick(this,"'+key+'");></div>');
                    wHTML += ('</div>');
                    wHTML += ('</div></div>');
             //   }
            }
            else{
                // big box design

                /* available equal to ZERO
                wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed flat no-shadow" id="'+boxid+'">');
                wHTML += ('<div class="info-circle-icon text-ultragray" style="margin-top: 60px;"><i class="'+domains[x].icon+'"></i></div>');
                wHTML += ('<h4 class="text-center text-gray">'+domains[x].name+'</h5>');
                wHTML += ('<h5 class="text-center text-gray" style="margin: 2px;">'+domains[x].description+'</h5>');
                wHTML += ('<h4 class="text-center text-red">COMING SOON</h4>');
                wHTML += ('<div class="box-footer-flatdown flat"><h5 class="text-center text-ultralight-blue" >info and settings</h5><p></p>');
                wHTML += ('<div class="switchOff"></div>');
                wHTML += ('</div>');
                wHTML += ('</div></div>');
                */
                //if ( domains[x].published == '1' ){
                    var key = domains[x].aiid;

                    if ( userActived[key] === false )
                        wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed flat no-shadow" id="'+boxid+'">');
                    else
                        wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed flat no-shadow borderActive" id="'+boxid+'">');

                    wHTML += ('<a><div class="info-circle-icon '+domains[x].widgetColor+'" style="margin-top: 60px;"><i class="'+domains[x].iconPath+'"></i></div></a>');
                    wHTML += ('<h4 class="text-center text-mute">'+domains[x].name+'</h4>');
                    wHTML += ('<h5 class="text-center text-gray">'+domains[x].description+'</h5>');

                    wHTML += addHtmlStarRating(userActived[key],boxid,domains[x].rating);

                    wHTML += ('<div class="box-footer-flatdown flat"><h5 class="text-center text-light-blue">info and settings</h5>');

                    if ( userActived[key] === false )
                        wHTML += ('<div class="switch" data-rnn="0" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"'+key+'");></div>');
                    else
                        wHTML += ('<div class="switch switchOn" data-rnn="1" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,"'+key+'");></div>');
                    wHTML += ('</div>');
                    wHTML += ('</div></div>');
               // }
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
            if (i==rating)
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" checked="checked">');
            else
                wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '">');
            wHTML += ('<label class="star-rating__ico fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="'+i+' out of '+i+' stars"></label>');
        }
    }else {
        for (var i = 5; i > 0; i--) {
            wHTML += ('<input class="star-rating__input" id="star-' + boxid + '-rating-' + i + '" type="radio" name="rating' + boxid + '" value="' + i + '" disabled="disabled">');
            wHTML += ('<label class="star-rating__ico_disabled fa fa-star-o fa-lg" for="star-' + boxid + '-rating-' + i + '" title="' + i + ' out of ' + i + ' stars"></label>');
        }

    }
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');
    return wHTML;
}


// Pass values to Modal on show dialog modal
$('#boxDomainInfo').on('show.bs.modal', function(e) {
    var curr_domain_name = $(e.relatedTarget).data('name').toUpperCase();
    var curr_domain_icon = $(e.relatedTarget).data('icon');
    var curr_domain_color = $(e.relatedTarget).data('color');

    $(e.currentTarget).find('span').text(curr_domain_name + ' - pre-trained Neural Network');
    $(e.currentTarget).find('i').attr('class', curr_domain_icon +' text-md text-white');
    $(e.currentTarget).find('.modal-header').attr('class', 'modal-header ' + curr_domain_color);
});
