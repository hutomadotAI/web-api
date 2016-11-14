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
        var node_nprompt = node.children[i].children[1].children[0].children[0];
        var node_prompt = node.children[i].children[2].children[0].children[0];
        var node_required = node.children[i].children[3].children[0].children[0];
        var list_prompt =  node_prompt.getAttribute('data-prompts');
        var prompts_split = list_prompt.split(',');

        var promptsArray = [];
        for (var j=0; j < prompts_split.length; j++)
            promptsArray.push(prompts_split[j]);

        v['entity_name']= node_entity.getAttribute('placeholder').replace(/[@]/g, "");
        v['n_prompts'] = node_nprompt.getAttribute('placeholder');
        v['required'] = node_required.checked;
        v['prompts'] = promptsArray;

        variables.push(v);
    }

    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnSaveIntent").prop("disabled", true);
    //resetMsgAlertIntentVariable();

    $.ajax({
        url: 'intentelement.php?intent=' + intentName,
        data: {
            intent_name: intentName, intent_prompts: expressions, intent_responses: responses,
            variables: variables
        },
        type: 'POST',/*
        error: function (xhr, ajaxOptions, thrownError) {
         alert(xhr.status + ' ' + thrownError);
         }*/
        success: function (result) {
            alert('success');

        },
        complete: function () {
            $("#btnSaveIntent").prop("disabled", false);
            document.body.style.cursor = prevCursor;
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

