var MENU = MENU || (function () {
        var _args = {}; // private
        return {
            init: function (Args) {
                _args = Args;

                // _args[0] -> ai_name
                // _args[1] -> label menu clicked
                // _args[2] -> level tree menu clicked
                // _args[3] -> block href link on clicked menu
                // _args[4] -> is menu limited
                // _args[0] == '' -> limited menu show during creation AI wizard or when start first time

                buildConsoleMenu(_args[0], _args[1], _args[2], _args[3], _args[4] );
                buildAccountMenu();
            }
        };
    }());

function buildConsoleMenu(ai_name, label_menu, level, block, limitedMenu) {
    const MAX_LENGTH_AINAME_TEXT_VISIBLE = 18;
    var newNode = document.createElement('ul');
    newNode.className = 'sidebar-menu';
    newNode.style = " padding-bottom:65px;";
    newNode.id = 'console-menu';
    var wHTML = "";
    
    wHTML += ('<li class="header unselectable" style="text-align: center;color:#8A8A8A;">CONSOLE</li>');
    wHTML += ('<li id="menu_home" class="unselectable"><a href="./home.php"><i class="fa fa-home text-light-blue" id="level0"></i><span>home</span></a></li>');

    if (!limitedMenu) {
        wHTML += ('<li class="unselectable" id="level1">');
        wHTML += ('<a href="#">');
        if (ai_name.length > MAX_LENGTH_AINAME_TEXT_VISIBLE)
            wHTML += ('<i class="fa fa-user text-olive"></i><span class="text-left" data-toggle="tooltip" title="' + ai_name + '">' + ai_name.substr(0, MAX_LENGTH_AINAME_TEXT_VISIBLE) + '</span><i class="fa fa-ellipsis-v pull-right"></i>');
        else
            wHTML += ('<i class="fa fa-user text-olive"></i><span class="text-left">' + ai_name + '</span><i class="fa fa-ellipsis-v pull-right"></i>');

        wHTML += ('</a>');
        wHTML += ('<ul class="treeview-menu">');
        wHTML += ('<li id="menu_training"><a href="./trainingAI.php" id="link_training"><i class="fa fa-graduation-cap"></i> <span>training</span></a></li>');
        wHTML += ('<li id="menu_entities"><a href="./entity.php" id="link_entities"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>');
        wHTML += ('<li id="menu_intents"><a href="./intent.php" id="link_intents"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>');
        wHTML += ('<li id="menu_settings"><a href="./settingsAI.php" id="link_settings"><i class="fa fa-gear text-red"></i> <span>settings</span></a></li>');
        wHTML += ('</ul>');
        wHTML += ('</li>');
    }

    var loc = "botstore.php";
    wHTML += ('<li class="unselectable" id="level2">');
    wHTML += ('<a href="#"><i class="fa fa-shopping-cart text-green"></i><span> botstore</span><i class="fa fa-ellipsis-v pull-right"></i></a>');
    wHTML += ('<ul class="treeview-menu">');
    wHTML += ('<li id="menu_botstore" ><a href="./' + loc + '"><i class="fa fa-globe text-gray text-center"></i> All</a></li>');
    for (var key in category_list) {
        var category = category_list[key];
        wHTML += ('<li id="menu_' + removeSpecialCharacters(category) + '" ><a href="' + loc + buildCategoryURIparameter(category) + '"><i class="fa ' + category_list_icons[key] + ' text-gray text-center"></i> ' + category + '</a></li>');
    }

    wHTML += ('</ul>');
    wHTML += ('</li>');

    wHTML += ('<li class="unselectable" id="level3">');
    wHTML += ('<a href="#">');
    wHTML += ('<i class="fa fa-book text-purple"></i><span>Documentation</span><i class="fa fa-ellipsis-v pull-right"></i>');
    wHTML += ('</a>');
    wHTML += ('<ul class="treeview-menu">');
    wHTML += ('<li id="menu_integrations"><a href="./integrationsAI.php" id="link_integrations"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>');
    wHTML += ('</ul>');
    wHTML += ('</li>');


    newNode.innerHTML = wHTML;
    document.getElementById('sidebarmenu').appendChild(newNode);

    document.getElementById('level' + level).className = 'active';
    document.getElementById('menu_' + removeSpecialCharacters(label_menu)).className = 'active';
    if (block)
        document.getElementById('link_' + label_menu).href = '#';
}


function buildAccountMenu() {
    var newNode = document.createElement('ul');
    newNode.className = 'sidebar-menu';
    newNode.id = 'account-menu';

    newNode.style = " position: absolute; bottom:0; width: 230px; min-height: 85px;";
    var wHTML = "";

    wHTML += ('<ul class="sidebar-menu" style=" background: #2e3032; position: absolute; bottom:0; width: 230px; min-height: 85px;">');
    wHTML += ('<li class="header" style="color:#8A8A8A;text-align: center;"><b>MY ACCOUNT</b></li>');
    wHTML += ('<li id="menu_logout"><a href="./logout.php" id="link_logout"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>');
    wHTML += ('</ul>');

    newNode.innerHTML = wHTML;
    document.getElementById('sidebarmenu').appendChild(newNode);
}

function buildCategoryURIparameter(category){
    return '?category='+ adjustURIEscapingCategoryValue(category);
}

function adjustURIEscapingCategoryValue(value){
    return value.replace('&', '%26').split(' ').join('%20');
}


function removeSpecialCharacters(str){
    return str.replace(/[&\/\\#,+()$~%.'":*?<>{}\s+]/g, '');
}