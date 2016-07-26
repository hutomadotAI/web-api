function showDomains(str,size){
  var wHTML = "";
  for (var x in domains) {
    var boxid = 'box' + domains[x].dom_id;
    if ( (str!=" ") && ( (str.length==0) || (domains[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
          if(size==0){

                  // slim box design
                  if ( domains[x].available == '0' ){
                        wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed" id="'+boxid+'"><p></p>');
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
                            wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed" id="'+boxid+'"><p></p>');
                        else
                            wHTML += ('<div class="col-xs-12"><div class="box box-solid box-default-small-fixed borderActive" id="'+boxid+'"><p></p>');
                        wHTML += ('<div class="col-xs-2">');
                        wHTML += ('<div class="info-circle-icon-small '+domains[x].color+'"><i class="'+domains[x].icon+'"></i></div>');
                        wHTML += ('</div>');
                        wHTML += ('<div class="col-xs-7">');
                        wHTML += ('<h4 class="text-center" style="text-align: left;">&nbsp;'+domains[x].name+'</h4>');
                        wHTML += ('<h5 class="text-center text-muted" style="text-align: left;">&nbsp;'+domains[x].description+'</h5>');
                        wHTML += ('<h5 class="text-center text-light-blue" style="text-align: left;">&nbsp;info and settings</h5>');
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

                      wHTML += ('<div class="info-circle-icon '+domains[x].color+'" style="margin-top: 60px;"><i class="'+domains[x].icon+'"></i></div>');
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

function domainsToJsonForPOST() {
    $("#btnSave").attr("disabled", true);
    $("#btnSave").attr("onClick","");
    $('#btnSave').removeClass('btn btn-success flat').addClass('btn btn-success flat disabled');

    $("#btnBack").attr("disabled", true);
    $("#btnBack").attr("onClick","");
    $('#btnBack').removeClass('btn btn-primary flat').addClass('btn btn-primary flat disabled');

    $("#btnSwitch").attr("disabled", true);
    $("#btnSwitch").attr("onClick","");

    var JsonStringActiveDomains = JSON.stringify(userActived);
    $("#userActivedDomains").attr("value", JsonStringActiveDomains);
}


