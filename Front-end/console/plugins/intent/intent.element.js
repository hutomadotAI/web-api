//document.getElementById("btnSaveEntity").addEventListener("click", saveIntent);

var variables = [];

function getMultipleElementValues(elementName, attributeName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(addEscapeCharacter(elements[i].getAttribute(attributeName)));
    }
    return values;
}

function addEscapeCharacter(value) {
    return value.replace(/,/g, "||#44;");
}

function removeEscapeCharacter(value) {
    return value.replace(/\|\|#44;/g, ",");
}


function saveIntent() {
    $(this).prop("disabled", true);

    var intentName = document.getElementById('intent-name').value;
    var expressions = getMultipleElementValues('user-expression-row', 'placeholder');
    var responses = getMultipleElementValues('intent-response-row', 'placeholder');
    var variables = [];

    var node = document.getElementById('parameter-list');
    var len = node.childNodes.length;

    for (var i = 0; i < len; i++) {
        var v = {};

        //*** check entity name
        var node_entity = node.children[i].children[0].children[0].children[0];
        if (node_entity.getAttribute('placeholder') == 'add entity') {
            msgAlertIntentVariable(2, 'Cannot save. Missing entity on row ' + (i + 1));
            return false;
        }
        v['entity_name'] = node_entity.getAttribute('placeholder').replace(/[@]/g, "");


        //*** check n prompt
        var node_nprompt = node.children[i].children[1].children[0].children[0];

        if (node_nprompt.value != '' && node_nprompt.value !== 'undefined') {
            if (inputValidation(node_nprompt.value, 'intent_n_prompt')) {
                msgAlertIntentVariable(2, 'Cannot save. The n_prompt must be a number between 1 to 99 at row ' + (i + 1));
                msgAlertIntentElement(2, 'Not saved!!');
                return false;
            }
            node_nprompt.setAttribute('placeholder', node_nprompt.value);
        }


        if (node_nprompt.getAttribute('placeholder') == 'n° prompt') {
            msgAlertIntentVariable(2, 'Cannot save. Missing n° prompt value on row ' + (i + 1));
            msgAlertIntentElement(2, 'Not saved!!');
            return false;
        }

        v['n_prompts'] = node_nprompt.getAttribute('placeholder');


        //*** check list prompts
        var node_prompt = node.children[i].children[2].children[0].children[0];
        var list_prompt = node_prompt.getAttribute('data-prompts');
        var prompts_split = list_prompt.split(',');

        var promptsArray = [];
        for (var j = 0; j < prompts_split.length; j++)
            promptsArray.push(removeEscapeCharacter(prompts_split[j]));
        v['prompts'] = promptsArray;

        //*** check required checkbox
        var node_required = node.children[i].children[3].children[0].children[0];
        v['required'] = node_required.checked;

        variables.push(v);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveIntent").prop("disabled", true);
    resetMsgAlertIntentVariable();

    msgAlertIntentElement(1, 'saving...');
    $.ajax({
        url: 'intentelement.php?intent=' + intentName,
        data: {
            intent_name: intentName, intent_prompts: expressions, intent_responses: responses,
            variables: variables
        },
        type: 'POST',
        success: function (result) {
            msgAlertIntentElement(4, 'Saved!!');

        },
        complete: function () {
            $("#btnSaveIntent").prop("disabled", false);
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            msgAlertIntentElement(2, 'Not saved!!');
        }
    });
}

$('#boxPrompts').on('show.bs.modal', function (e) {
    var parent = $(e.relatedTarget).parent().parent().parent();

    //send to modal current entity name selected from first node in the current variables row selected
    var node_entity = parent.children().children().children();
    var curr_entity = node_entity.attr('placeholder');
    $(e.currentTarget).find('input[name="curr_entity"]').val(curr_entity);

    //send to modal current intent store in data-intent html
    var curr_intent = $(e.relatedTarget).data('intent');
    $(e.currentTarget).find('input[name="curr_intent"]').val(curr_intent);

    //send to modal current n prompt value or placeholder if is not changed from second node in the current variables row selected
    var node_n_prompts = parent.children().eq(1).children().children();
    var curr_n_prompts;
    if (node_n_prompts.val() == '' || node_n_prompts.val() == 'n° prompt')
        curr_n_prompts = node_n_prompts.attr('placeholder');
    else
        curr_n_prompts = node_n_prompts.val();
    $(e.currentTarget).find('input[name="curr_n_prompts"]').val(curr_n_prompts);

    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    cleanupromptDialogbox();
    loadPromptsForEntity(curr_entity)
});

