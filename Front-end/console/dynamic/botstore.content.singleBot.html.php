<?php
$bot['badge']='Top Developer';
$bot['badgeIcon']='fa fa-rebel';
$bot['users']='103';
$bot['price']='0.0';
$bot['msg']='Questi contenuti non sono disponibili in Italiano. Leggi ulteriori informazioni sulle lingue supportate.';
$bot['sample']='User: I want to sleep.<p></p>Agent: Need a pick-me-up? I can find somewhere nearby to get some coffee.<p></p><p></p>User: You\'re so sweet.<p></p>Agent: I like you too. You\'re a lot of fun to talk to.';
$bot['lastUpdate']='10 september 2016';
$bot['classification']='entertainment';
$bot['version']='2.1';
$bot['developer']='hu:toma Ltd.';
$bot['company']='HUTOMA';
$bot['contact']='support@hutoma.com';
$bot['address']='Carrer del Consell de Cent, 341';
$bot['postcode']='08007';
$bot['city']='Barcelona';
$bot['nation']='Spain';
$bot['siteLink']='http://www.hutoma.com';
$bot['permissionLink']='https://www.google.com/permissions/';
$bot['privacyLink']='https://www.google.it/intl/it/policies/privacy/';
$bot['reportLink']='./botstore.php';

$bot['longDescription']='A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart. I am alone, 
and feel the charm of existence in this spot, which was created for the bliss of souls like mine.
I am so happy, my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents.
I should be incapable of drawing a single stroke at the present moment
and yet I feel that I never was a greater artist than now.';

function licenceTypeToString($x){
    switch ($x) {
        case 0:
            return 'Trial';
        case 1:
            return 'Subscription';
        case 2:
            return 'Perpetual';
    }
}

function rangeActivation($n){
     switch (true) {
         case ($n < 10):
             return '0-10';
         case ($n < 100):
             return '10-100';
         case ($n < 1000):
             return '100-1000';
         case ($n < 5000):
             return '1.000-5.000';
         case ($n < 10000):
             return '5.000-10.000';
         case ($n < 20000):
             return '10.000-20.000';
         case ($n < 50000):
             return '20.000-50.000';
         case ($n < 100000):
             return '50.000-100.000';
         case ($n < 500000):
             return '100.000-500.000';
         case ($n < 1000000):
             return '500.000-1.000.000';
         case ($n < 5000000):
             return '1.000.000-5.000.000';
         case ($n < 10000000):
             return '5.000.000-10.000.000';
     }
}
?>

<div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">

    <?php include './dynamic/botstore.content.singleBot.card.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.video.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.description.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.footer.html.php'; ?>
    
</div>


