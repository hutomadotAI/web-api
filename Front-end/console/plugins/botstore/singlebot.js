// Pass values to Modal on show dialog modal
$('#boxBotStoreInfo').on('show.bs.modal', function(e) {
    var curr_bot_name = $(e.relatedTarget).data('name').toUpperCase();
    var curr_bot_description = $(e.relatedTarget).data('description');

    var curr_bot_icon ='fa fa-user';
    var curr_bot_color = 'gray';
    // TODO need to have from getAI specific usecase
    var curr_bot_usescase = 'This AI is used for la bla bla bla bla bla';
    // TODO need to have from getAI more details
    var curr_bot_details= 'bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ' +
        'bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla';
    /*
     if ( $(e.relatedTarget).data('iconPath') != '' )
     curr_domain_icon = $(e.relatedTarget).data('iconPath');
     if ( $(e.relatedTarget).data('widgetColor') !='')
     curr_domain_color = $(e.relatedTarget).data('widgetColor');
     */
    $(e.currentTarget).find('span').text(curr_bot_name);
    $(e.currentTarget).find('h3').text(curr_bot_description);
    $(e.currentTarget).find('dd').text(curr_bot_details);
    $(e.currentTarget).find('df').text(curr_bot_usescase);

    $(e.currentTarget).find('i').attr('class', curr_bot_icon +' text-md text-gray');
    $(e.currentTarget).find('.modal-header').attr('class', 'modal-header ' + curr_bot_color);
});
