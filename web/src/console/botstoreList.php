<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";

//header('P3P: CP="CAO PSA OUR"');

$header_page_title = "Botstore";
include __DIR__ . "/include/page_head_default.php";
?>

<body class="hold-transition skin-blue fixed " onload="scrollTo(0,0)">
<?php include_once __DIR__ . "/../console/common/google_analytics.php"; ?>

<!-- ================ PAGE CONTENT ================= -->
<section class="content">
    <div class="overlay carousel-ovelay" id="carousel-overlay">
        <i class="fa fa-refresh fa-spin center-block"></i>
    </div>
    <p id="botsCarousels"></p>
    <?php include __DIR__ . '/dynamic/botstore.content.singleBot.buy.html.php'; ?>
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