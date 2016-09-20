document.getElementById("btnNext").addEventListener("click", wizardNext);
//document.getElementById("btnBack").addEventListener("click", backPage);


function wizardNext() {
    $(this).prop("disabled",true);
    $("#btnCancel").prop("disabled",true);
    $("#domsearch").prop("disabled",true);

    if(document.domainsNewAIform.onsubmit) {
        return;
    }

    RecursiveUnbind($('#wrapper'));
    var JsonStringActiveDomains = JSON.stringify(userActived);
    $("#userActivedDomains").attr("value", JsonStringActiveDomains);
    document.domainsNewAIform.submit();
}

function backPage(){
    $(this).prop("disabled",true);
    history.go(-1);
    return false;
}



function showDomains(str,size){
  var wHTML = "";
  for (var x in domains) {
    var boxid = 'box' + domains[x].dom_id;
    if ( (str!=" ") && ( (str.length==0) || (domains[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
          if(size==0){
                  // slim box design
                  if ( domains[x].available == '0' ){
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed flat no-shadow" id="'+boxid+'"><p></p>');
                        wHTML += ('<div class="col-xs-2">');
                        wHTML += ('<div class="info-circle-icon-small text-ultragray"><i class="'+domains[x].icon+'"></i></div>');
                        wHTML += ('</div>');
                        wHTML += ('<div class="col-xs-7">');
                        wHTML += ('<h4 class="text-center text-center text-gray" style="text-align: left;">&nbsp;'+domains[x].name+'</h4>');
                        wHTML += ('<h5 class="text-center text-center text-gray" style="text-align: left;">&nbsp;'+domains[x].description+'</h5>');
                        wHTML += ('<h4 class="text-center text-ultralight-blue" style="text-align: left;">&nbsp;COMING SOON</h4>');
                        wHTML += ('</div>');
                        wHTML += ('<div class="col-xs-3">');
                        wHTML += ('<div class="switchOff" style="margin-top:33px;"></div>');
                        wHTML += ('</div>');       
                        wHTML += ('</div></div>');
                    }
                    else{
                        var key = domains[x].dom_id;
                     
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
                        wHTML += ('<a data-toggle="modal"  data-target="#detailsDomain" data-id="'+domains[x].dom_id+'" style="cursor: pointer;">');
                        wHTML += ('<h5 class="text-center text-light-blue" style="text-align: left;">&nbsp;info and settings</h5>');
                        wHTML += ('</a>');
                        wHTML += ('</div>');
                        wHTML += ('<div class="col-xs-3">');

                        if ( userActived[key] === false )
                              wHTML += ('<div class="switch" box-checked="0" style="margin-top:33px;" onclick=switchClick(this,'+key+');></div>');
                        else
                              wHTML += ('<div class="switch switchOn" box-checked="1" style="margin-top:33px;" onclick=switchClick(this,'+key+');></div>');
                        wHTML += ('</div>');
                        wHTML += ('</div></div>');
                    }
          }
          else{
                  // big box design
                  if ( domains[x].available == '0' ){

                      wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed" id="'+boxid+'">');
                      wHTML += ('<div class="info-circle-icon text-ultragray" style="margin-top: 60px;"><i class="'+domains[x].icon+'"></i></div>');
                      wHTML += ('<h4 class="text-center text-gray">'+domains[x].name+'</h5>');
                      wHTML += ('<h5 class="text-center text-gray" style="margin: 2px;">'+domains[x].description+'</h5>');
                      wHTML += ('<h4 class="text-center text-red">COMING SOON</h4>');
                      wHTML += ('<div class="box-footer-flatdown"><h5 class="text-center text-ultralight-blue" >info and settings</h5><p></p>');
                      wHTML += ('<div class="switchOff"></div>');
                      wHTML += ('</div>');       
                      wHTML += ('</div></div>');
                  }
                  else{
                      var key = domains[x].dom_id;

                      if ( userActived[key] === false )
                          wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed" id="'+boxid+'">');
                      else
                          wHTML += ('<div class="col-md-3 col-sm-4 col-xs-6"><div class="box box-solid box-default-fixed borderActive" id="'+boxid+'">');

                      wHTML += ('<a><div class="info-circle-icon '+domains[x].color+'" style="margin-top: 60px;"><i class="'+domains[x].icon+'"></i></div></a>');
                      wHTML += ('<h4 class="text-center text-mute">'+domains[x].name+'</h4>');
                      wHTML += ('<h5 class="text-center text-gray">'+domains[x].description+'</h5>');
                      wHTML += ('<div class="box-footer-flatdown"><h5 class="text-center text-light-blue">info and settings</h5>');

                      if ( userActived[key] === false )
                          wHTML += ('<div class="switch" box-checked="0" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,'+key+');></div>');
                      else
                          wHTML += ('<div class="switch switchOn" box-checked="1" id="btnSwitch" style="margin-top:10px;" onclick=switchClick(this,'+key+');></div>');
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
    var boxid = 'box' + key;
    $(node).toggleClass('switchOn');
    if( $(node).attr('box-checked') == 0) {
        $(node).attr('box-checked', 1);
        $("#"+boxid).addClass("borderActive");
        userActived[key] = true;
    }
    else {
        $(node).attr('box-checked', 0);
        userActived[key] = false;
        $("#"+boxid).removeClass("borderActive");
    }
}

function msgAlertNewDomains(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertNewDomains").attr('class','alert alert-dismissable flat alert-base');
            $("#icongAlertNewDomains").attr('class', 'icon fa fa-check');
            break;
        case 1:
            $("#containerMsgAlertNewDomains").attr('class','alert alert-dismissable flat alert-warning');
            $("#icongAlertNewDomains").attr('class', 'icon fa fa-check');
            break;
        case 2:
            $("#containerMsgAlertNewDomains").attr('class','alert alert-dismissable flat alert-danger');
            $("#icongAlertNewDomains").attr('class', 'icon fa fa-warning');
            break
    }
    document.getElementById('msgAlertNewDomains').innerText = msg;
}
