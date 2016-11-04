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

        case 'intent' :         letters = /^[a-zA-Z0-9\-_]+$/;                  break;
        case 'user_expression': letters = /^[a-zA-Z0-9\-_]+$/;                  break;
        case 'prompt' :         letters = /^[a-zA-Z0-9\-_.,?!']+$/;             break;
        case 'n_prompt':        letters = /^\d{1,2}$/;                          break;
        case 'response' :       letters = /^[a-zA-Z0-9\-_.,?!']+$/;             break;

        default:
    }

    //if (letters.test(txt))
    if(txt.match(letters))
        return false;

    return true;
}


