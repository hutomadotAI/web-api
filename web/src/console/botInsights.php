<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/analyticsApi.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

$intervalString = "";
if (isset($_REQUEST['from'])) {
    $fromDate = date('Y-m-d');
    $fromDateIso = utils::toIsoDate($_REQUEST['from']);
    $intervalString = 'from ' . $fromDate;
} else {
    $fromDate = date('Y-m-d', strtotime('-30 days'));
    $fromDateIso = \hutoma\utils::toIsoDate($fromDate);
}

if (isset($_REQUEST['to'])) {
    $toDate = date('Y-m-d', $_REQUEST['to']);
    $toDateIso = utils::toIsoDate($_REQUEST['to']);
    $intervalString = $intervalString . ' to ' . $toDate;
} else {
    $toDate = date('Y-m-d');
    $toDateIso = \hutoma\utils::toIsoDate($toDate);
}

if (empty($intervalString)) {
    $intervalString = '(last 30 days)';
}

$aiid = sessionObject::getCurrentAI()['aiid'];


$header_page_title = "Bot Insights";
$header_additional_entries = "<style>
        .datepicker table tr td.today {
            background-color: #324d64;
        }

        .datepicker table tr td.disabled,
        .datepicker table tr td.disabled:hover {
            background: none;
            color: #666;
            cursor: default;
        }
    </style>";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <?php include __DIR__ . '/dynamic/insights.chatlogs.download.php'; ?>
                </div>
            </div>

            <div class="row">

                <script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

                <script src="<?php $assets->getAsset('insights/insights.js') ?>"></script>
                <script type="text/javascript">
                    google.charts.load("current", {packages: ['corechart']});
                    requestInsightsSessions('<?php echo $aiid?>', '<?php echo $fromDateIso ?>', '<?php echo $toDateIso ?>', '<?php echo $intervalString ?>');
                    requestInsightsInteractions('<?php echo $aiid?>', '<?php echo $fromDateIso ?>', '<?php echo $toDateIso ?>', '<?php echo $intervalString ?>');

                </script>

                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow unselectable">
                        <div class="box-header with-border">
                            <i class="fa fa-bar-chart-o text-green"></i>
                            <div class="box-title"><b>Charts</b></div>
                        </div>
                        <div class="box-body">

                            <div style="width:90%; height: 300px; display: block; margin: auto; text-align: center;">
                                <div id="chartSessionsLoading" style="height:100%; width:100%">
                                    <span style="display: inline-block; height:50%"></span><img src="dist/img/loader_grey.gif" style="vertical-align: middle;">
                                </div>
                                <div id="chartSessions"></div>
                                <div id="chartSessionsError" style="width:100%; height:100%; border:1px solid red; display: none;">
                                    <span style="line-height:150px">There was an error loading the data for Chat Sessions. Please try again later.</span>
                                </div>
                            </div>

                            <div style="width:90%; height: 300px; display: block; margin: auto; text-align: center; vertical-align: middle">
                                <div id="chartInteractionsLoading" style="height:100%; width:100%">
                                    <span style="display: inline-block; height:50%"></span><img src="dist/img/loader_grey.gif" style="vertical-align: middle;"></div>
                                <div id="chartInteractions"></div>
                                <div id="chartInteractionsError" style="width:100%; height:100%; border:1px solid red; display: none;">
                                    <span style="line-height:150px">There was an error loading the data for Chat Interactions. Please try again later.</span>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
    </section>
</div>

    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="<?php $assets->getAsset('shared/shared.js') ?>"></script>
<script src="/console/dist/vendors/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>

<script src="<?php $assets->getAsset('messaging/messaging.js') ?>"></script>

<?php
$menuObj = new menuObj(sessionObject::getCurrentAI()['name'], "insights", 1, true, false);
include __DIR__ . "/include/page_menu_builder.php" ?>

<script>

</script>

</body>
</html>
