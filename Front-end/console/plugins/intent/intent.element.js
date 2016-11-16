//document.getElementById("btnSaveEntity").addEventListener("click", saveIntent);

var variables = [];

function getMultipleElementValues(elementName, attributeName) {
    var values = [];
    var elements = document.getElementsByName(elementName);
    for (var i = 0; i < elements.length; i++) {
        values.push(elements[i].getAttribute(attributeName));
    }
    return values;
}

function saveIntent() {
    $(this).prop("disabled", true);

    var intentName  = document.getElementById('intent-name').value;
    var expressions = getMultipleElementValues('user-expression-row', 'placeholder');
    var responses   = getMultipleElementValues('intent-response-row', 'placeholder');
    var variables = [];

    var node = document.getElementById('parameter-list');
    var len = node.childNodes.length;

    for (var i = 0; i < len; i++) {
        var v = {};

        var node_entity = node.children[i].children[0].children[0].children[0];
        if ( node_entity.getAttribute('placeholder') == 'add entity'){
            containerMsgAlertIntentVariable(2, 'Cannot save. Missing entity on row '+(i+1));
            return false;
        }
        v['entity_name']= node_entity.getAttribute('placeholder').replace(/[@]/g, "");


        var node_nprompt = node.children[i].children[1].children[0].children[0];
        if ( node_nprompt.getAttribute('placeholder') == 'n° prompt'){
            containerMsgAlertIntentVariable(2, 'Cannot save. Missing n° prompt value on row '+i+1);
            return false;
        }

        if (inputValidation(node_nprompt.getAttribute('placeholder'), 'intent_n_prompt')) {
            msgAlertIntentPrompt(2, 'The n_prompt needs contain only number with max two digit');
            return false;
        }

        v['n_prompts'] = node_nprompt.getAttribute('placeholder');


        var node_prompt = node.children[i].children[2].children[0].children[0];
        var list_prompt =  node_prompt.getAttribute('data-prompts');
        var prompts_split = list_prompt.split(',');
        var promptsArray = [];
        for (var j=0; j < prompts_split.length; j++)
            promptsArray.push(prompts_split[j]);
        v['prompts'] = promptsArray;


        var node_required = node.children[i].children[3].children[0].children[0];
        v['required'] = node_required.checked;


        variables.push(v);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveIntent").prop("disabled", true);
    resetMsgAlertIntentVariable();

    msgAlertIntentElement(1,'saving...');
    $.ajax({
        url: 'intentelement.php?intent=' + intentName,
        data: {
            intent_name: intentName, intent_prompts: expressions, intent_responses: responses,
            variables: variables
        },
        type: 'POST',
        success: function (result) {
            msgAlertIntentElement(4,'Saved!!');

        },
        complete: function () {
            $("#btnSaveIntent").prop("disabled", false);
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            msgAlertIntentElement(2,'Not saved!!');
        }
    });
}

$('#boxPrompts').on('show.bs.modal', function (e) {
    var curr_entity = $(e.relatedTarget).data('entity');
    var curr_intent = $(e.relatedTarget).data('intent');
    var curr_n_prompts = $(e.relatedTarget).data('nprompts');

    $(e.currentTarget).find('input[name="curr_entity"]').val(curr_entity);
    $(e.currentTarget).find('input[name="curr_intent"]').val(curr_intent);
    $(e.currentTarget).find('input[name="curr_n_prompts"]').val(curr_n_prompts);
   
    // remove character @
    curr_entity = curr_entity.replace(/[@]/g, "");

    cleanupromptDialogbox();
    loadPromptsForEntity(curr_entity)
});

