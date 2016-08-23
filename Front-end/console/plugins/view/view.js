function sendAIID(aiid){
    var input = document.createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("name", "aiid");
    input.setAttribute("value", aiid);

    document.getElementById("viewAllAIsform").appendChild(input);
    document.getElementById("viewAllAIsform").submit();
}

function publishAI(){
    if ( this.className == 'btn btn-info flat pull-right'){
        this.className = 'btn btn-warning flat pull-right';
        this.innerHTML = '<i class="fa fa-globe"></i> Unpublish AI';
    }
    else{
        this.className = 'btn btn-info flat pull-right';
        this.innerHTML = '<i class="fa fa-globe"></i> Publish AI';
    }
}
