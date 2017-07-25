<?php
header('P3P: CP="CAO PSA OUR"');
session_start();
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Botstore</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    <?php include_once "./dynamic/hotjar.inc.php" ?>
</head>
<body class="hold-transition skin-blue fixed " onload="scrollTo(0,0)">
<?php include_once "../console/common/google_analytics.php"; ?>

<!-- ================ PAGE CONTENT ================= -->
<section class="content">
    <div class="overlay carousel-ovelay" id="carousel-overlay">
        <i class="fa fa-refresh fa-spin center-block"></i>
    </div>
    <p id="botsCarousels"></p>
    <?php include './dynamic/botstore.content.singleBot.buy.html.php'; ?>
</section>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="scripts/external/select2/select2.full.js"></script>

<script src="./scripts/botstore/botstoreWizard.js"></script>
<script src="./scripts/botstore/botstore.js"></script>
<script src="./scripts/botstore/carousel.js"></script>
<script src="./scripts/botcard/botcard.js"></script>
<script src="./scripts/botcard/buyBot.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>

<?php
$category = isset($_GET['category']) ? $_GET['category'] : "";
$showHeader = isset($_GET['showHeader']) ? $_GET['showHeader'] : 'true';
$openFullStore = isset($_GET['openFullStore']) ? $_GET['openFullStore'] : 'false';
?>
<script>
    $(document).ready(function () {
        getCarousels(
            '<?php echo $category ?>',
            DRAW_BOTCARDS.BOTSTORE_WITH_BOT_FLOW.value,
            <?php echo $showHeader?>,
            <?php echo $openFullStore?>);
    });
    $('#buyBot').on('hide.bs.modal', function (e) {
        var purchase_state = document.getElementById('purchase_state').value;
        if (purchase_state === 'true') {
            switchCard(document.getElementById('bot_id').value, DRAW_BOTCARDS.BOTSTORE_FLOW.value);
        }
    });
    <?php
    unset($aiName);
    unset($category);
    unset($isExistAiId);
    ?>
</script>
</body>
</html>