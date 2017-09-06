<?php

include_once __DIR__ . '/../common/sessionObject.php';

    $userDetails = \hutoma\sessionObject::isLoggedIn() ? $_SESSION[$_SESSION['navigation_id']]['user_details'] : false;

    // Checking if all values are present to prevent JS errors
    // 
    // Please forgive me for this atrocity below
    if ($userDetails['email'] && $userDetails['name'] && $userDetails['id'] && $userDetails['created']) {
?>
    <script>
        var dataLayer = window.dataLayer || [];

        var user = {
            email: '<?php echo $userDetails['email']; ?>',
            name: '<?php echo $userDetails['name']; ?>',
            user_id: '<?php echo $userDetails['id']; ?>',
            created_at: <?php echo strtotime($userDetails['created']) ;?>
        }

        dataLayer.push({ 
            'user': user 
        })

    </script>

<?php 
    } // atrocity II THE END
?>

<!-- Google Tag Manager -->
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-WQRP6QZ');</script>
<!-- End Google Tag Manager -->