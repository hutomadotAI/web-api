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

$('#buyBot').on('hide.bs.modal', function (e) {
    var purchase_state = document.getElementById('purchase_state').value;
    if (purchase_state == 1)
        switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.BOTSTORE_FLOW.value);
});