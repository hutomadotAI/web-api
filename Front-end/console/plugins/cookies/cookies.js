function setCookie (nome, valore, scadenza) {
    if (scadenza == "") {
        var oggi = new Date();

        oggi.setMonth(oggi.getMonth() + 3);
        scadenza = oggi.toGMTString();
    }
    valore = escape(valore);
    document.cookie=nome + "=" + valore + ";expires=" + scadenza;
}

function deleteCookie(nome){
  setCookie(nome,'',-1);
}

function valueCookie (nome) {
   var valore = document.cookie;
   var inizioCookie = valore.indexOf(" " + nome + "=");
  
   if (inizioCookie == -1) 
      inizioCookie = valore.indexOf(nome + "=");
   if (inizioCookie == -1)
      valore = null;
   if (inizioCookie >= 0) { 
      inizioCookie = valore.indexOf("=", inizioCookie) + 1; 
      var fineCookie = valore.indexOf(";", inizioCookie); 

      if (fineCookie == -1)  
         fineCookie = valore.length;

      valore = unescape(valore.substring(inizioCookie, fineCookie)); 
   }
   return valore;
}

function isCookieEnabled () {
    var ris = false;
    setCookie("testCookie", "test"); 
    if (valueCookie("testCookie") == "test")
        ris = true; 
    return ris;
}

function checkCookiesToRedirect(){
    if (isCookieEnabled)
        window.location.replace(window.location.pathname + '/empty.php');
}

