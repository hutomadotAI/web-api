/**
 * Created by Hutoma on 14/10/16.
 */

function limitText(limitField, limitNum) {
    if (limitField.val().length < 1)
        return -1;
    if (limitField.val().length >= limitNum) {
        limitField.val(limitField.val().substring(0, limitNum-1));
        return 1;
    }
    return 0;
}

function isNameExists(name,list_name){
    for (var x in list_name) {
        if(name === list_name[x])
            return true;
    }
    return false;
}

function inputValidation(txt,field) {
    var letters;
    switch(field){

        case 'ai_name' :        letters = /^[a-zA-Z0-9\-_\s]+$/;                break;
        case 'ai_description' : letters = /^[a-zA-Z0-9\-_.,?!+()£$%&@'\s]+$/;   break;
        case 'entity_name' :    letters = /^[a-zA-Z0-9\-_]+$/;                  break;
        case 'entity_value' :   letters = /^[a-zA-Z0-9\-_\s]+$/;                break;
        case 'intent_name' :    letters = /^[a-zA-Z0-9\-_]+$/;                  break;
        case 'intent_response': letters = /^[a-zA-Z0-9\-_.,?!'\s]+$/;           break;
        case 'intent_prompt':   letters = /^[a-zA-Z0-9\-_.,?!'\s]+$/;           break;
        case 'user_expression': letters = /^[a-zA-Z0-9\-_]+$/;                  break;
        case 'intent_n_prompt': letters = /^([0]?[1-9]{1,2})$/;                 break;
        case 'response' :       letters = /^[a-zA-Z0-9\-_.,?!']+$/;             break;

        case 'bot_name' :       letters = /^[a-zA-Z0-9\-_.,:\s]+$/;                            break;
        case 'bot_description': letters = /^[a-zA-Z0-9\-_.,?!+()£$%&@'\s]+$/;               break;
        case 'bot_licence_fee': letters = /^([0-9]{0,2}((.)[0-9]{0,2}))$/;   break;
        case 'bot_version' :    letters = /^\d{1,2}\.\d{1,2}\.\d{1,2}$/;     break;

        case 'developer_name' :   letters = /^[a-zA-Z0-9\-_.:,\s]+$/;     break;
        case 'developer_address': letters = /^[a-zA-Z0-9\-_.,\s]+$/;   break;
        case 'developer_city' :   letters = /^[a-zA-Z0-9\-_\s]+$/;     break;
        case 'developer_country': letters = /^[a-zA-Z0-9\-_\s]+$/;     break;
        case 'developer_company': letters = /^[a-zA-Z0-9\-_.,\s]+$/;     break;
        case 'developer_email' :  letters = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/; break;

        default:
    }
    
    if(txt.match(letters))
        return false;

    return true;
}


