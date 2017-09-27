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

function toJSDate($dateParts) {
    return 'new Date(' . $dateParts[0] . ',' . ($dateParts[1] - 1) . ',' . $dateParts[2] . ')';
}

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

if (!isset($intervalString)) {
    $intervalString = 'last 30 days';
}

$fromDateParts = explode('-', $fromDate);
$toDateParts = explode('-', $toDate);

$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$api = new api\analyticsApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$sessions = $api->getChatSessions($aiid, $fromDateIso, $toDateIso);
$interactions = $api->getChatInteractions($aiid, $fromDateIso, $toDateIso);
unset($pi);

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

                <?php if (sizeof($interactions->objects) > 0 && sizeof($sessions->objects) > 0) { ?>

                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">

                    function getChatOptions(title, color) {
                        return {
                            title: title,
                            width: "90%",
                            height: 300,
                            bar: {groupWidth: "35"},
                            legend: {position: "none"},
                            backgroundColor: {fill: 'transparent'},
                            vAxis: {
                                textStyle: {color: '#999999'},
                                format: 'short',
                                gridlines: {color: '#404040'}
                            },
                            hAxis: {
                                textStyle: {color: '#999999'},
                                gridlines: {color: "#404040"},
                                baselineColor: '#808080',
                                minValue: (<?php echo toJSDate($fromDateParts) ?>),
                                maxValue: (<?php echo toJSDate($toDateParts) ?>)
                            },
                            titleTextStyle: {color: 'white', fontName: 'Helvetica', fontSize: '16px'},
                            colors: [color]
                        };
                    }

                    google.charts.load("current", {packages: ['corechart']});
                    google.charts.setOnLoadCallback(drawChart);
                    function drawChart() {
                        var dataSessions = google.visualization.arrayToDataTable([
                            ["Date", "Sessions"],
                            <?php
                            foreach ($sessions->objects as $entry) {
                                $date = explode('-', date('Y-m-d', strtotime($entry->date)));
                                echo '[' . toJSDate($date) . ', ' . $entry->count . '],';
                            }
                            ?>
                        ]);

                        var dataInteractions = google.visualization.arrayToDataTable([
                            ["Date", "Interactions"],
                            <?php
                            foreach ($interactions->objects as $entry) {
                                $date = explode('-', date('Y-m-d', strtotime($entry->date)));
                                echo '[' . toJSDate($date) . ', ' . $entry->count . '],';
                            }
                            ?>
                        ]);

                        <?php if ($sessions != null) {?>
                        var chartSessions = new google.visualization.ColumnChart(document.getElementById("chartSessions"));
                        chartSessions.draw(dataSessions, getChatOptions('Chat sessions per day (<?php echo $intervalString?>)', '#ffa31a'));
                        <?php }
                        if ($interactions != null) {?>
                        var chartInteractions = new google.visualization.ColumnChart(document.getElementById("chartInteractions"));
                        chartInteractions.draw(dataInteractions, getChatOptions('Chat interactions per day (<?php echo $intervalString?>)', '#4d94ff'));
                        <?php } ?>
                    }
                </script>

                <div class="col-md-12">
                <div class="box box-solid box-clean flat no-shadow unselectable">
                <div class="box-header with-border">
                    <i class="fa fa-bar-chart-o text-green"></i>
                    <div class="box-title"><b>Charts</b></div>
                </div>
                <div class="box-body"
                <div id="chartSessions"></div>
                <div id="chartInteractions"></div>
                    <?php } else { ?>
                    <div class="col-md-12">
                        <div class="box box-solid box-clean flat no-shadow unselectable">
                            <div class="box-header with-border">
                        No data for the period: <?php echo $intervalString ?>
                    <?php } ?>
                            </div></div></div>
                </div>
                </div>
            </div>
    </section>
</div>

    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
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
    var todayDate = new Date();
    $('#chatlogsDateFrom').datepicker({
        autoclose: true,
        format: 'yyyy-mm-dd',
        todayBtn: 'linked',
        todayHighlight: true,
        endDate: todayDate
    })
        .on('changeDate', function (selected) {
            var minDate = new Date(selected.date.valueOf());
            $('#chatlogsDateTo').datepicker('setStartDate', minDate);
        });
    $('#chatlogsDateTo').datepicker({
        autoclose: true,
        format: 'yyyy-mm-dd',
        todayBtn: 'linked',
        endDate: "0d",
        todayHighlight: true
    });
</script>
</body>
</html>
