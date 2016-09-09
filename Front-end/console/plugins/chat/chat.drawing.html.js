var drawHTML = drawHTML || (function(){
        var _args = {}; // private
        return {
            init : function(Args) {
                _args = Args;
                drawChatFooter(_args[0],_args[1]);
                drawMenuOptionVoice();
            }
        };
    }());

