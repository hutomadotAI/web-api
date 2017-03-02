var form = document.getElementById("deleteForm");

document.getElementById("modalDelete").addEventListener("click", function () {
    form.submit();
});

document.getElementById("btnModelCancel").addEventListener("click", function () {
    $('#btnDelete').prop("disabled",false);
    $('#btnCancel').prop("disabled",false);
});

document.getElementById("btnModelClose").addEventListener("click", function () {
    $('#btnDelete').prop("disabled",false);
    $('#btnCancel').prop("disabled",false);
});


$('#deleteAI').on('show.bs.modal', function (e) {
    $('#btnDelete').prop("disabled",true);
    $('#btnCancel').prop("disabled",true);
    $('#btnSave').prop("disabled",true);
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-ai-label');
    var elemBtn = document.getElementById('modalDelete');
    var deleteBtn = document.getElementById('btnDelete');
    var value = deleteBtn.value;

    elem.innerHTML = 'Are you sure you want to permanently delete <label>' + value + '</label>? ';
    elemBtn.setAttribute("value", esseyId);
});
