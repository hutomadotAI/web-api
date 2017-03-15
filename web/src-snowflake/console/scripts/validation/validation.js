/**
 * Created by Hutoma on 14/10/16.
 */
validation = {};

function limitText (limitField, limitNum) {
    if (limitField.val().length < 1)
        return -1;
    if (limitField.val().length >= limitNum) {
        limitField.val(limitField.val().substring(0, limitNum-1));
        return 1;
    }
    return 0;
}
validation.limitText = function(limitField, limitNum) {
    return limitText(limitField, limitNum);
};

function isNameExists(name,list_name){
    for (var x in list_name) {
        if(name === list_name[x])
            return true;
    }
    return false;
}
validation.isNameExists = function(name, list_name) {
    return isNameExists(name, list_name);
};

function isInputInvalid(txt,field) {
    var letters;
    switch(field){

        case 'ai_name' :        letters = /^[a-zA-Z0-9\-_\s]+$/;                break;
        case 'ai_description' : letters = /^[a-zA-Z0-9\-_.,?!+()£$%&@'\s]+$/;   break;

        default:
    }

    if(txt.match(letters))
        return false;

    return true;
}

validation.isInputInvalid = function(txt,field) {
    return isInputInvalid(txt,field);
};
