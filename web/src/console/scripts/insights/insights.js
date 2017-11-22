
function isoDateToJsDate(isoDate) {
    var date = new Date(isoDate);
    var month = date.getMonth() + 1;
    var year = date.getFullYear();
    var day = date.getDate();

    return new Date(year, month, day);
}

function requestInsightsSessions(aiid, fromDateIso, toDateIso, intervalString) {

    google.charts.setOnLoadCallback(function () {

        function showError() {
            document.getElementById('chartInteractionsLoading').style.display = 'none';
            document.getElementById('chartInteractionsError').style.display = 'inline-block';
        }

        document.getElementById('chartSessionsLoading').style.display = 'block';

        var request = {
            url: './proxy/insightsProxy.php',
            data: {
                aiid: aiid,
                fromDateIso: fromDateIso,
                toDateIso: toDateIso,
                dataType: 'sessions'
            },
            verb: 'POST',
            onGenericError: function (statusMessage) { showError(); },
            onOK: function (response) {
                drawChart(response, 'Sessions', fromDateIso, toDateIso, 'chartSessions',
                    'chartSessionsLoading', 'Chat sessions per day ' + intervalString, '#ffa31a');
            },
            onShowError: function (message) { showError(); }
        };
        commonAjaxApiRequest(request);
    });
}

function drawChart(response, metricName, fromDateIso, toDateIso, chartElement, loadingElement, label, chartColor) {
    if (response !== null && response.objects !== null) {
        var data = new google.visualization.DataTable();
        data.addColumn('datetime', 'Date');
        data.addColumn('number', metricName);

        var length = response.objects.length;
        data.addRows(length);
        for (var i = 0; i < length; i++) {
            data.setCell(i, 0, isoDateToJsDate(response.objects[i].date));
            data.setCell(i, 1, response.objects[i].count);
        }
        var chart = new google.visualization.ColumnChart(document.getElementById(chartElement));
        google.visualization.events.addListener(chart, 'ready', function () {
            document.getElementById(loadingElement).style.display = 'none';
        });
        chart.draw(data, getChartOptions(label, chartColor, fromDateIso, toDateIso));
    }
}

function requestInsightsInteractions(aiid, fromDateIso, toDateIso, intervalString) {

    google.charts.setOnLoadCallback(function () {

        document.getElementById('chartInteractionsLoading').style.display = 'block';

        function showError() {
            document.getElementById('chartInteractionsLoading').style.display = 'none';
            document.getElementById('chartInteractionsError').style.display = 'inline-block';
        }

        var request = {
            url: './proxy/insightsProxy.php',
            data: {
                aiid: aiid,
                fromDateIso: fromDateIso,
                toDateIso: toDateIso,
                dataType: 'interactions'
            },
            verb: 'POST',
            onGenericError: function (statusMessage) { showError(); },
            onOK: function (response) {
                drawChart(response, 'Interactions', fromDateIso, toDateIso, 'chartInteractions',
                    'chartInteractionsLoading', 'Chat interactions per day ' + intervalString, '#4d94ff');
            },
            onShowError: function (message) { showError(); }
        };
        commonAjaxApiRequest(request);
    });
}

function getChartOptions(title, color, minValueDate, maxValueDate) {
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
            minValue: isoDateToJsDate(minValueDate),
            maxValue: isoDateToJsDate(maxValueDate)
        },
            titleTextStyle: {color: 'white', fontName: 'Helvetica', fontSize: '16px'},
            colors: [color]
        };
}

function setupLogDownloadDatePickers() {
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
}

$(document).ready(function(){
    setupLogDownloadDatePickers();

});