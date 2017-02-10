function showIntegrations(str){
    var wHTML = "";

    for (var x in integrations) {
        if ( (str!=" ") && ( (str.length==0) || (integrations[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
       
            wHTML += ('<div class="box-body flat" style="background-color: #404446; border: 1px solid #202020;padding: 5px 5px 5px 5px;">');
            wHTML += ('<div class="row">');
            wHTML += ('<div class="col-xs-1">');
            wHTML += ('<img src="./dist/img/social/icons/'+integrations[x].icon+'" width="30" height="30" alt="'+integrations[x].name+'">');
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-4" style="padding-top:4px;">');
            if ( integrations[x].available != '0' )
                wHTML += ('<span class="text-md">'+integrations[x].name+'</span>');
            else
                wHTML += ('<span class="text-md text-muted">'+integrations[x].name+'</span>')
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-4" style="padding-top:8px;">');
            wHTML += ('<span class="text pull-left">'+integrations[x].description+'</span>');
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-3">');
            if ( integrations[x].available != '0' ){
                wHTML += ('<a data-toggle="collapse" data-parent="#accordion" href="#collapse'+(integrations[x].name).replace(" ","")+'">');
                wHTML += ('<button class="btn btn-success pull-right text-sm flat no-margin" style="margin-right: 5px; margin-top: 3px; width:130px;"><i class="fa fa-download"></i> View more info</button>');
                wHTML += ('</a>');
            }
            else
                wHTML += ('<button class="btn btn-primary pull-right text-sm text-white flat no-margin" style="margin-right: 5px; margin-top: 3px; width:130px;"><i class="fa fa-info-circle" style="padding-right:3px;"></i> Coming soon</button>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</a>');
            wHTML += ('</div>');
            wHTML += ('<div id="collapse'+(integrations[x].name).replace(" ","")+'" class="panel-collapse collapse">');
            wHTML += ('<div class="box-body ">');
            wHTML += ('<section class="invoice">');
            wHTML += ('</section>');
            wHTML += ('</div>');
            wHTML += ('</div>');
          
        
    }
  }
  newNode.innerHTML = wHTML;
  document.getElementById('intsearch').appendChild(newNode);
}