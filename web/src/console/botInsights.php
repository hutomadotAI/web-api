<?php

require "../pages/config.php";
require_once "api/botstoreApi.php";
require_once "api/analyticsApi.php";
require_once "common/utils.php";

if (!\hutoma\console::checkSessionIsActive()) {
    exit;
}
if (isset($_REQUEST['from'])) {
    $from = \hutoma\utils::toIsoDate($_REQUEST['from']);
    $intervalString = '(from ' . date('Y-m-d');
} else {
    $from = \hutoma\utils::toIsoDate(date('Y-m-d', strtotime('-30 days')));
}

if (isset($_REQUEST['to'])) {
    $to = hutoma\utils::toIsoDate($_REQUEST['to']);
    $intervalString = $intervalString . ' to ' . date('Y-m-d', $_REQUEST['to']) . ')';
} else {
    $to = \hutoma\utils::toIsoDate(date('Y-m-d'));
}

if (!isset($intervalString)) {
    $intervalString = '(last 30 days)';
}

$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];
$api = new \hutoma\api\analyticsApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$sessions = $api->getChatSessions($aiid, $from, $to);
$interactions = $api->getChatInteractions($aiid, $from, $to);
unset($pi);

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Bot Insights </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
    <script src="scripts/external/autopilot/autopilot.js"></script>
    <style>
        .datepicker table tr td.today {
            background-color: #324d64;
        }

        .datepicker table tr td.disabled,
        .datepicker table tr td.disabled:hover {
            background: none;
            color: #666;
            cursor: default;
        }
    </style>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <?php include './dynamic/insights.chatlogs.download.php'; ?>
                </div>
            </div>

            <div class="row">
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">

                    function getChatOptions(title, color) {
                        return {
                            title: title,
                            width: "90%",
                            height: 300,
                            bar: {groupWidth: "95%"},
                            legend: {position: "none"},
                            backgroundColor: {fill: 'transparent'},
                            vAxis: {textStyle: {color: '#999999'}, format: 'short', gridlines: {color: '#999999'}},
                            hAxis: {
                                textStyle: {color: '#999999'},
                                gridlines: {color: "#999999"},
                                baselineColor: '#999999'
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
                                echo '["' . date('Y-m-d', strtotime($entry->date)) . '", ' . $entry->count . '],';
                            }
                            ?>
                        ]);

                        var dataInteractions = google.visualization.arrayToDataTable([
                            ["Date", "Interactions"],
                            <?php
                            foreach ($interactions->objects as $entry) {
                                echo '["' . date('Y-m-d', strtotime($entry->date)) . '", ' . $entry->count . '],';
                            }
                            ?>
                        ]);

                        <?php if ($sessions != null) {?>
                        var chartSessions = new google.visualization.ColumnChart(document.getElementById("chartSessions"));
                        chartSessions.draw(dataSessions, getChatOptions('Chat sessions per day <?php echo $intervalString?>', '#ffa31a'));
                        <?php }
                        if ($interactions != null) {?>
                        var chartInteractions = new google.visualization.ColumnChart(document.getElementById("chartInteractions"));
                        chartInteractions.draw(dataInteractions, getChatOptions('Chat interactions per day <?php echo $intervalString?>', '#4d94ff'));
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
                </div>
                </div>
            </div>
    </section>
</div>

<footer class="main-footer">
    <?php include './dynamic/footer.inc.html.php'; ?>
</footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/messaging/messaging.js"></script>

<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "insights", 1, true, false]);
    </script>
</form>

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
