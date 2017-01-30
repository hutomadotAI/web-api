function showIntegrations(str){
    var wHTML = "";

    for (var x in integrations) {
        if ( (str!=" ") && ( (str.length==0) || (integrations[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
       
            wHTML += ('<div class="box-body flat" style="background-color: #404446; border: 1px solid #202020;">');
            wHTML += ('<div class="row">');
            wHTML += ('<div class="col-xs-1">');
            wHTML += ('<img src="./dist/img/social/icons/'+integrations[x].icon+'" width="30" height="30" alt="'+integrations[x].name+'">');
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-3">');
            if ( integrations[x].available != '0' )
                wHTML += ('<label><span class="lead text">'+integrations[x].name+'</span></label>');
            else
                wHTML += ('<label><span class="lead text-muted">'+integrations[x].name+'</span></label>')
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-4">');
            wHTML += ('<h5><span class="text pull-left">'+integrations[x].description+'</span></h5>');
            wHTML += ('</div>');
            wHTML += ('<div class="col-xs-4">');
            if ( integrations[x].available != '0' ){
                wHTML += ('<a data-toggle="collapse" data-parent="#accordion" href="#collapse'+(integrations[x].name).replace(" ","")+'">');
                wHTML += ('<button class="btn btn-success pull-right text-sm flat" style="margin-right: 5px; margin-top: 3px; width:130px;"><i class="fa fa-download"></i> View more info</button>');
                wHTML += ('</a>');
            }
            else
                wHTML += ('<button class="btn btn-warning pull-right text-sm flat disabled" style="margin-right: 5px; margin-top: 3px; width:130px;"><i class="fa fa-exclamation-triangle"></i> Coming soon</button>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</a>');
            wHTML += ('</div>');
            wHTML += ('<div id="collapse'+(integrations[x].name).replace(" ","")+'" class="panel-collapse collapse">');
            wHTML += ('<div class="box-body ">');
            wHTML += ('<section class="invoice">');
            wHTML += ('Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably have not heard of them accusamus labore sustainable VHS.');
            wHTML += ('</section>');
            wHTML += ('</div>');
            wHTML += ('</div>');
          
        
    }
  }
  newNode.innerHTML = wHTML;
  document.getElementById('intsearch').appendChild(newNode);
}