(function ($) {
    /* The coolie policy bar takes a map of
     * options (for further extensibility)
     */
    $.cookiePolicyBar = function (options) {

        var defaultOptions = {
            // Message to be displayed
            message: "We use cookies to give you the best online experience. By using our website you agree to our use of cookies in accordance with our cookie policy.",
            // Accept button text
            acceptButtonText: 'Ok',
            // Decline button text (empty for no decline button)
            declineButtonText: 'Disable Cookies',
            // Policy link text (empty for no policy link)
            policyText: 'cookie policy',
            // Policy link
            policyUrl: '/cookiepolicy.pdf',
            // Policy link window target
            policyUrlTarget: '_self',
            // How many days to keep the user chosen preference (both accept or decline)
            cookieExpirationInDays: 60,
            // Set the domain for invalidating the cookies
            domain: String(window.location.hostname),
            // Cookie policy name
            cookieName: 'cookiePolicy',
            // Whether to force the bar to always show or not regardless of the cookie
            // Note that this will force it to show even if the user has accepted/declined
            // which can be annoying...
            forceShow: false
        };

        var options = $.extend(defaultOptions, options);

        // Function to remove the bar
        var removeBar = function () {
            $('#cookiePolicyBar').hide(0, function () {
                $('#cookiePolicyBar').remove();
            });
        };

        // Function to acknowledge the user accepting cookie storage
        var cookiePolicyAccept = function (expirationDate) {
            setCookie(options.cookieName, 'accepted', expirationDate);
            removeBar();
        };

        // Function to acknowledge the user declining cookie storage
        // and removal of existing cookies for the domain
        var cookiePolicyDecline = function (expirationDate) {
            // set the expiration date to be yesterday
            var yesterday = new Date();
            yesterday.setTime(yesterday.getTime() - (1000 * 60 * 60 * 24));
            yesterday = yesterday.toUTCString();
            // Now invalidate all the cookies
            var allCookies = document.cookie.split('; ');
            for (var i = 0; i < allCookies.length; i++) {
                var c = allCookies[i].split('=');
                if (c[0].indexOf('_') >= 0) {
                    document.cookie = c[0] + '=0; expires=' + yesterday + '; domain=' + options.domain.replace('www', '') + '; path=/';
                } else {
                    document.cookie = c[0] + '=0; expires=' + yesterday + '; path=/';
                }
            }
            // Set the cookie to declined so we don't ask again
            // Interestingly we're setting a cookie after the user has declined usage of cookies,
            // but this falls into the exemption policy based on
            // http://ec.europa.eu/justice/data-protection/article-29/documentation/opinion-recommendation/files/2012/wp194_en.pdf
            setCookie(options.cookieName, 'declined', expirationDate);

            removeBar();
        };

        // Function to set a cookie value and expiration date
        var setCookie = function (cookieName, cookieValue, expireDate) {
            document.cookie = cookieName + '=' + cookieValue + ';expires=' + expireDate + ';path=/';
            return true;
        }

        //Retrieves current cookie preference
        var existingCookieValue = '';
        var docCookies = document.cookie.split('; ');
        for (var i = 0; i < docCookies.length; i++) {
            var c = docCookies[i].split('=');
            if (c[0] == options.cookieName) {
                existingCookieValue = c[1];
            }
        }

        // Bail out if we had already set the cookie previously and it's still valid
        if (existingCookieValue != '' && !options.forceShow) {
            return (existingCookieValue == 'accepted');
        }

        // Sets expiration date for cookie
        var cookieExpirationDate = new Date();
        cookieExpirationDate.setTime(cookieExpirationDate.getTime() + (options.cookieExpirationInDays * 1000 * 60 * 60 * 24));
        cookieExpirationDate = cookieExpirationDate.toUTCString();

        // Create the bar
        var barElement = '<div id="cookiePolicyBar" style="text-align: left; padding:4px;margin-top: -10px;">'
            + '<span class="cookiePolicyBar-message">' + options.message + '</span> '
            + '<a class="cookiePolicyBar-accept" href="">' + options.acceptButtonText + '</a> '
            + (options.declineButtonText != '' ? '<a class="cookiePolicyBar-decline" href="">' + options.declineButtonText + '</a> ' : '')
            + (options.policyText != '' ? '<a class="cookiePolicyBar-policy" href="' + options.policyUrl + '" target="' + options.policyUrlTarget
            + '">' + options.policyText + '</a> ' : '');
        $('body').append(barElement);

        $('#cookiePolicyBar .cookiePolicyBar-accept').click(function () {
            cookiePolicyAccept(cookieExpirationDate);
            return true;
        });
        $('#cookiePolicyBar .cookiePolicyBar-decline').click(function () {
            cookiePolicyDecline(cookieExpirationDate);
            return false;
        });
    }

})(jQuery);