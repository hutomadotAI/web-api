function wizardNext() {
    $(this).prop('disabled',true);
    $('#btnBack').prop('disabled',true);
    $('#domsearch').prop('disabled',true);

    if(document.domainsNewAIform.onsubmit) {
        return;
    }

    RecursiveUnbind($('#wrapper'));
    var JsonStringActiveDomains = JSON.stringify(userActived);
    document.getElementById('userActivedDomains').value = JsonStringActiveDomains
    document.domainsNewAIform.submit();
}

function backPage(){
    document.domainsNewAIformGoBack.action = './newAI.php';
    document.domainsNewAIformGoBack.submit();
}

function showDomains(str,size){
    var wHTML = "";
    for (var x in domains) {
        var boxid = 'rnn' + domains[x].domainId;
        if ( (str!=" ") && ( (str.length==0) || (domains[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
            if(size==0){
                // slim box design
                if ( domains[x].available == '1' ){
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

                    var key = domains[x].domainId;

                    if ( userActived[key] === false )
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow" id="'+boxid+'"><p></p>');
                    else
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow borderActive" id="'+boxid+'"><p></p>');
                    wHTML += ('<div class="col-xs-2">');
                    wHTML += ('<div class="info-circle-icon-small '+domains[x].color+'"><i class="'+domains[x].icon+'"></i></div>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-7">');
                    wHTML += ('<h4 class="text-center" style="text-align: left;">&nbsp;'+domains[x].name+'</h4>');
                    wHTML += ('<h5 class="text-center text-muted" style="text-align: left;">&nbsp;'+domains[x].description+'</h5>');
                    wHTML += ('<a data-toggle="modal" ' +
                    'data-target="#boxDomainInfo" ' +
                    'data-id="'+domains[x].domainId+'" ' +
                    'data-name="'+domains[x].name+'" ' +
                    'data-icon="'+domains[x].icon+'" ' +
                    'data-color="'+domains[x].color+'" ' +
                    'style="cursor: pointer;">');
                    wHTML += ('<h5 class="text-center text-light-blue" style="text-align: left;">&nbsp;info and settings</h5>');
                    wHTML += ('</a>');
                    wHTML += ('</div>');
                    wHTML += ('<div class="col-xs-3">');

                    if ( userActived[key] === false )
                        wHTML += ('<div class="switch" data-rnn="0" style="margin-top:33px;" onclick=switchClick(this,'+key+');></div>');
                    else
                        wHTML += ('<div class="switch switchOn" data-rnn="1" style="margin-top:33px;" onclick=switchClick(this,'+key+');></div>');
                    wHTML += ('</div>');
                    wHTML += ('</div></div>');
                }
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
                if ( domains[x].available == '1' ){
                    var key = domains[x].domainId;

                    if ( userActived[key] === false )
                        wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed flat no-shadow" id="'+boxid+'">');
                    else
                        wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed flat no-shadow borderActive" id="'+boxid+'">');

                    wHTML += ('<a><div class="info-circle-icon '+domains[x].color+'" style="margin-top: 60px;"><i class="'+domains[x].icon+'"></i></div></a>');
                    wHTML += ('<h4 class="text-center text-mute">'+domains[x].name+'</h4>');
                    wHTML += ('<h5 class="text-center text-gray">'+domains[x].description+'</h5>');
                    wHTML += ('<div class="box-footer-flatdown flat"><h5 class="text-center text-light-blue">info and settings</h5>');

                    if ( userActived[key] === false )
                        wHTML += ('<div class="switch" data-rnn="0" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,'+key+');></div>');
                    else
                        wHTML += ('<div class="switch switchOn" data-rnn="1" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,'+key+');></div>');
                    wHTML += ('</div>');
                    wHTML += ('</div></div>');
                }
            }
        }
    }
    newNode.innerHTML = wHTML;
    document.getElementById('domsearch').appendChild(newNode);
}

function switchClick(node,key){
    var boxid = 'rnn' + key;
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

// Pass values to Modal on show dialog modal
$('#boxDomainInfo').on('show.bs.modal', function(e) {
    var curr_domain_name = $(e.relatedTarget).data('name').toUpperCase();
    var curr_domain_icon = $(e.relatedTarget).data('icon');
    var curr_domain_color = $(e.relatedTarget).data('color');

    $(e.currentTarget).find('span').text(curr_domain_name + ' - pre-trained Neural Network');
    $(e.currentTarget).find('i').attr('class', curr_domain_icon +' text-md text-white');
    $(e.currentTarget).find('.modal-header').attr('class', 'modal-header ' + curr_domain_color);
});
