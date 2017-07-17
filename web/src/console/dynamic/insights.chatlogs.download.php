<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <i class="fa fa-cloud-download text-green"></i>
        <div class="box-title"><b>Download chat logs</b></div>
    </div>

    <form action="./dynamic/downloadChatLogs.php" method="post">
    <div class="box-body" id="boxIntents">
        <table style="width:100%">
            <tr>
                <td><label for="inputLabelFrom">From date:</label></td>
                <td>
                    <div class="input-group date" id="chatlogsDateFrom">
                        <input type="text" class="form-control" name="from" id="inputLabelFrom"><span class="input-group-addon"><i
                                    class="glyphicon glyphicon-th"></i></span>
                    </div>
                </td>
                <td>
                    <div style="width:100px;"></div>
                </td>
                <td><label for="inputLabelTo">To date:</label></td>
                <td>
                    <div class="input-group date" id="chatlogsDateTo">

                        <input type="text" class="form-control" name="to" id="inputLabelTo"><span class="input-group-addon"><i
                                    class="glyphicon glyphicon-th"></i></span>
                    </div>
                </td>
                <td>
                    <div style="width:100px;"></div>
                </td>
                <td>
                    <div class="input-group-btn" tabindex="0">
                        <button type="submit" id="btnDownloadChatlogs" class="btn btn-success flat" style="width: 120px;">Download Logs
                        </button>
                    </div>
                </td>
            </tr>
        </table>
        <p></p>
    </div>
    </form>

    <div class="box-footer">
        <span>
            If you’re stuck email <a href='mailto:support@hutoma.com?subject=Invite%20to%20slack%20channel'
                                     tabindex="-1">support@hutoma.com</a> for an invite to our slack channel.
        </span>
        <p></p>

    </div>

</div>


