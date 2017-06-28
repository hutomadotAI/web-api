

<div class="box-header with-border">
    <i class="fa fa-facebook text-blue"></i>
    <div class="box-title"><b>Facebook</b></div>
    <a data-toggle="collapse" href="#collapseEntitiesInfo">
        <div class=" pull-right">more info
            <i class="fa fa-question-circle text-sm text-yellow"></i>
        </div>
    </a>
</div>

<div id="facebookState">
</div>

<script>
    $( document ).ready(function() {
        loadFacebookAction();
    });

    var appid = "unknown";

    function facebookConnect() {
        <?php // https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow  ?>
        var facebookRedir = htmlEncode(window.location.href.split('#')[0]);
        document.cookie = "facebookRedir=" + htmlEncode(facebookRedir) + "; expires=0; path=/";
        var fbLogin = "https://www.facebook.com/v2.9/dialog/oauth"
            + "?client_id=" + appid
            + "&scope=manage_pages,pages_show_list,pages_messaging,public_profile"
            + "&redirect_uri=" + facebookRedir;
        window.location.href = fbLogin;
    }

    function loadFacebookAction(action, params) {
        $("#facebookState").html("Loading...");
        if (action != null) {
            params["action"] = action;
        }
        var paramStr = (params == null)? "" : "?" + jQuery.param(params);
        $("#facebookState").load("./dynamic/integrations.facebook.content.html.php" + paramStr);
    };

    $("#facebookState").on( "click", "#fb-renew", function() {
        loadFacebookAction("renew", {});
        return false;
    });

    $("#facebookState").on( "click", "#fb-disconnect", function() {
        loadFacebookAction("disconnect", {});
        return false;
    });

    $("#facebookState").on( "click", ".fb-page-list", function() {
        var selectedPage = $(this).attr("id").substr(4);
        loadFacebookAction("page", {"id" : selectedPage});
        return false;
    });

    $("#facebookState").on( "click", "#fb-int-connect", function() {
        facebookConnect();
        return false;
    });



</script>