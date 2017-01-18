var MENU = MENU || (function () {
        var _args = {}; // private
        return {
            init: function (Args) {
                _args = Args;

                // _args[0] -> ai_name
                // _args[1] -> label menu clicked
                // _args[2] -> level tree menu clicked
                // _args[3] -> block href link on clicked menu
                // _args[0] == '' -> limited menu show during creation AI wizard or when start first time

                if (!(_args[0] === ''))
                    buildConsoleMenu(_args[0], _args[1], _args[2], _args[3], _args[5]);
                else
                    buildLimitedConsoleMenu(_args[1]);

                buildAccountMenu();
            }
        };
    }());

function buildConsoleMenu(ai_name, label_menu, level, block, deep_level_name) {
    var newNode = document.createElement('ul');
    newNode.className = 'sidebar-menu';
    newNode.id = 'console-menu';
    var wHTML = "";

    wHTML += ('<li class="header unselectable" style="text-align: center;color:#8A8A8A;">CONSOLE</li>');
    wHTML += ('<li id="menu_home" class="unselectable"><a href="./home.php"><i class="fa fa-home text-light-blue" id="level0"></i><span>home</span></a></li>');

    wHTML += ('<li class="unselectable" id="level1">');
    wHTML += ('<a href="#">');
    wHTML += ('<i class="fa fa-user text-olive"></i><span>' + ai_name + '</span><i class="fa fa-ellipsis-v pull-right"></i>');
    wHTML += ('</a>');
    wHTML += ('<ul class="treeview-menu">');
    wHTML += ('<li id="menu_training"><a href="./trainingAI.php" id="link_training"><i class="fa fa-graduation-cap"></i> <span>training</span></a></li>');
    wHTML += ('<li id="menu_intents"><a href="./intent.php" id="link_intents"><i class="fa fa-commenting-o text-green"></i> <span>intents</span></a></li>');
    wHTML += ('<li id="menu_entities"><a href="./entity.php" id="link_entities"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>');

    wHTML += ('<li id="menu_settings"><a href="./settingsAI.php" id="link_settings"><i class="fa fa-gear text-red"></i> <span>settings</span></a></li>');
    wHTML += ('</ul>');
    wHTML += ('</li>');

    wHTML += ('<li class="unselectable" id="level2">');
    /*
    wHTML += ('<li id="menu_entities"><a href="./entity.php" id="link_entities"><i class="fa fa-sitemap text-yellow"></i> <span>entities</span></a></li>');
    wHTML += ('</li>');
    */

    if ((deep_level_name === '' || deep_level_name === undefined)) {
        wHTML += ('<li class="unselectable" id="menu_botstore"><a href="./botstore.php" id="link_botstore"><i class="fa fa-shopping-cart text-green"></i><span class="unselectable"> botstore</span></a></li>');
        wHTML += ('</li>');
    }
    else {

        var loc = "'./botstore.php'";

        wHTML += ('<li class="unselectable" id="level3">');
        wHTML += ('<a href="#"><span><i class="fa fa-shopping-cart text-green"></i><span onClick="window.location.href ='+loc+'"> botstore</span></span></a>');
        wHTML += ('<ul class="treeview-menu">');
        wHTML += ('<li id="menu_bot"><a href="#" id="link_bot"><i class="fa fa-arrow-circle-o-right text-sm text-white"></i>'+deep_level_name+'</a></li>');
        wHTML += ('</ul>');
        wHTML += ('</li>');
        label_menu = 'bot';
    }


    wHTML += ('<li class="unselectable" id="level4">');
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
    document.getElementById('menu_' + label_menu).className = 'active';
    if (block)
        document.getElementById('link_' + label_menu).href = '#';
}

function buildLimitedConsoleMenu(label_menu) {
    var newNode = document.createElement('ul');
    newNode.className = 'sidebar-menu';
    newNode.id = 'console-menu';
    var wHTML = "";
    wHTML += ('<li class="header unselectable" style="text-align: center;color:#8A8A8A;">CONSOLE</li>');
    wHTML += ('<li id="menu_home"><a href="./home.php"><i class="fa fa-home text-light-blue"></i><span>home</span></a></li>');

    wHTML += ('<li class="unselectable" id="level3">');
    wHTML += ('<a href="#">');
    wHTML += ('<i class="fa fa-book text-purple"></i><span>documentation</span><i class="fa fa-ellipsis-v pull-right"></i>');
    wHTML += ('</a>');
    wHTML += ('<ul class="treeview-menu">');
    wHTML += ('<li id="menu_integrations"><a href="./integrationsAI.php" id="link_integrations"><i class="glyphicon glyphicon-list-alt text-default"></i>integrations</a></li>');
    wHTML += ('</ul>');
    wHTML += ('</li>');

    newNode.innerHTML = wHTML;
    document.getElementById('sidebarmenu').appendChild(newNode);
    document.getElementById('menu_' + label_menu).className = 'active';
}

function buildAccountMenu() {
    var newNode = document.createElement('ul');
    newNode.className = 'sidebar-menu';
    newNode.id = 'account-menu';

    newNode.style = " position: absolute; bottom:0; width: 230px; min-height: 135px;";
    var wHTML = "";

    wHTML += ('<ul class="sidebar-menu" style=" background: #2e3032; position: absolute; bottom:0; width: 230px; min-height: 85px;">');
    wHTML += ('<li class="header" style="color:#8A8A8A;text-align: center;"><b>MY ACCOUNT</b></li>');
    wHTML += ('<li id="menu_logout"><a href="./logout.php" id="link_logout"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>');
    wHTML += ('</ul>');

    newNode.innerHTML = wHTML;
    document.getElementById('sidebarmenu').appendChild(newNode);
}