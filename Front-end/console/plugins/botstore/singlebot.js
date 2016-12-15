document.getElementById("btnPayment").addEventListener("click", purchaseBot);


function purchaseBot() {
    $("#btnBuyBot").removeClass('btn-success').addClass('btn-primary');
    $("#btnBuyBot").removeAttr('data-toggle');
    $("#btnBuyBot").removeAttr('data-target');
    $("#btnBuyBot").html('<b>Bot purchased </b><span class="fa fa-check-circle-o"></span>');
}
