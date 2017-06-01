function infoSidebarMenu(label) {
    var v = [];
    switch (label) {
        case 'home' :
            v['menu_label'] = label;
            v['menu_level'] = 0;
            v['menu_block'] = false;
            break;
        case 'settings' :
            v['menu_label'] = label;
            v['menu_level'] = 1;
            v['menu_block'] = false;
            break;
        case 'botstore' :
            v['menu_label'] = 'botstore';
            v['menu_level'] = 2;
            v['menu_block'] = false;
            break;
        default:
    }
    return v;
}