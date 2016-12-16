<?php
require_once "./common/bot.php";

// TODO remove hardcoded part
$bot = new \hutoma\bot();


$bot->setName($tmp_bot['name']);
$bot->setDescription($tmp_bot['description']);
$bot->setIconPath($tmp_bot['iconPath']);
$bot->setWidgetColor($tmp_bot['widgetColor']);
$bot->setLicenceFee($tmp_bot['licenceFee']);
$bot->setLicenceType($tmp_bot['licenceType']);
$bot->setRating($tmp_bot['rating']);

$bot->setBadge('Top Developer');
$bot->setBadgeIcon('fa fa-rebel');
$bot->setUsers('103');

$bot->setAlarmMessage('Questi contenuti non sono disponibili in Italiano. Leggi ulteriori informazioni sulle lingue supportate. treyerhgwie rgnised fgnsdif gnsipd fgnsidfww');

$bot->setUsecase('User: I want to sleep.<p></p>Agent: Need a pick-me-up? I can find somewhere nearby to get some coffee.<p></p><p></p>User: You\'re so sweet.<p></p>Agent: I like you too. You\'re a lot of fun to talk to.');
$bot->setUpdate('10 september 2016');
$bot->setCategory('entertainment');
$bot->setClassification('EVERYONE');
$bot->setVersion('2.1');
$bot->developer->setName('hu:toma Ltd.');
$bot->developer->setCompany('HUTOMA');
$bot->developer->setEmail('support@hutoma.com');
$bot->developer->setAddress('Carrer del Consell de Cent, 341');
$bot->developer->setPostcode('08007');
$bot->developer->setCity('Barcelona');
$bot->developer->setCountry('Spain');
$bot->developer->setWebsite('http://www.hutoma.com');

$bot->setPermissionLink('https://www.google.com/permissions/');
$bot->setPrivacyLink('https://www.google.it/intl/it/policies/privacy/');
$bot->setReport('./botstore.php');

$bot->setLongDescription('A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart. I am alone, 
and feel the charm of existence in this spot, which was created for the bliss of souls like mine.
I am so happy, my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents.
I should be incapable of drawing a single stroke at the present moment
and yet I feel that I never was a greater artist than now.');
?>

<div class="box box-solid box-clean flat no-shadow bot-box" id="singleBot">

    <?php include './dynamic/botstore.content.singleBot.card.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.video.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.description.html.php'; ?>

    <?php include './dynamic/botstore.content.singleBot.footer.html.php'; ?>
    
</div>

<?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>