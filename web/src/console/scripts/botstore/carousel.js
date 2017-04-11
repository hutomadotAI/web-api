function showCarousel(botstoreItems) {
    var wHTML = "";
    wHTML += '<section class="carousel-content" style="padding-right: 0px;">';
    wHTML += '<div class="carousel-box">';
    wHTML += '<div class="row no-margin">';
    wHTML += '<div class="col-md-6"><div class="carousel-title  pull-left"> Most Recent Chatbots </div></div>';
    wHTML += '<div class="col-md-6"><button id="seemore" class="btn btn-primary flat pull-right carousel-see-more" onCLick=""><b>see more</b></button></div>';
    wHTML += '</div>';
    wHTML += ('<div class="row carousel-row no-margin" id="bot_list">');
    for (var x in botstoreItems) {
        var bot = JSON.parse(botstoreItems[x]);

        var botId = bot['metadata']['botId'];
        var botName = bot['metadata']['name'];
        var botImagePath = bot['metadata']['imagePath'];
        var botPrice = bot['metadata']['price'];
        var botAuthor = bot['developer']['company'];
        var openBotDetails = 'onClick=openCardDetails(this,"' + botId + '");';
        var botOwned = bot['owned'];

        wHTML += '<span id="card' + botId + '" data-pos="' + x + '">';
        wHTML += '<div class="box-card card flat no-padding col-xs-6 col-sm-4 col-md-3 col-lg-1">';
        wHTML += '<img class="card-icon unselectable" src="img\\icons\\' +  botImagePath + '"' + openBotDetails +'>';
        wHTML += '<div class="card-title unselectable no-shadow"' + openCardDetails +'>';
        wHTML += botName;
        wHTML += '</div>';
        wHTML += '<div class="card-author unselectable no-shadow">';
        wHTML += 'by ' + botAuthor;
        wHTML += '</div>';
        wHTML += '<div class="card-footer flat unselectable">';
        wHTML += '<div class="row no-margin">';
        wHTML += '<div class="pull-left">';

        // TODO when API in ready we can add this infos
        //wHTML += '<i class="fa fa-star card-star"></i>';
        //wHTML += '<span class="card-users text-left">'+ bot['metadata']['activations']+'</span>';

        wHTML += '</div>';

        wHTML += ('<span class="card-linked" data-botid = "' + botId + '" data-linked="">');
        if (botOwned) {
            wHTML += ('<div class="card-purchased pull-right">');
            wHTML += ('purchased');
            wHTML += ('</div>');
        }
        else {
            wHTML += ('<div class="card-price pull-right">');
            wHTML += (botPrice + ' &#8364');
            wHTML += ('</div>');
        }

        wHTML += '</span>';
        wHTML += '</div>';
        wHTML += '</div>';
        wHTML += '</div>';
        wHTML += '</span>';
    }
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</section>');
    document.getElementById('botsCarousel').innerHTML = wHTML;
}


function openCardDetails(elem, botId) {
    elem.setAttribute('onClick', '');
    var form = document.createElement("form");
    document.body.appendChild(form);
    form.method = "POST";
    form.action = "./botcardDetail.php";

    var element = document.createElement("INPUT");
    element.name = "botId";
    element.value = botId;
    element.type = 'hidden';
    form.appendChild(element);
    form.submit();
}