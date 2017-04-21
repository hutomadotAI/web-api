function infoSidebarMenu(title) {
    var v = [];
    switch (title) {
        case 'home' :
            v['menu_title'] = title;
            v['menu_level'] = 0;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;
            break;
        case 'settings' :
            v['menu_title'] = title;
            v['menu_level'] = 1;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;
            break;
        case 'botstore' :

            v['menu_title'] = 'botstore';
            v['menu_level'] = 2;
            v['menu_block'] = false;
            v['menu_active'] = false;
            v['menu_deep'] = 0;
            break;
        default:
    }
    return v;
}