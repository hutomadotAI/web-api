<form method="POST" name="publishDeveloperForm" action="./dynamic/updateDeveloper.php">

<div class="box box-solid box-clean flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <div class="box-title"><b>Developer Info</b></div>
        <a data-toggle="collapse"  href="#collapseInfoDeveloper">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-sm text-yellow"></i>
            </div>
        </a>
    </div>
    <div class="box-body" id="boxDeveloper">

        <div class="alert alert-dismissable flat alert-warning" id="containerMsgAlertDomainsNewAIInfo" style="padding-bottom: 25px;">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
            <span id="msgAlertDomainsNewAIInfo" >
                <dt>Hutoma Publish</dt>
                <dd>
                   This is your first publich. It needs to add a Developer infos.
                </dd>
            </span>
        </div>

        <div id="collapseInfoDeveloper" class="panel-collapse collapse">
            <div class="box-body no-margin no-padding">
                <div class="overlay center-block">
                    <section class="content-info" >
                        <div class="box-body">
                            The developer infos needs to BLA BLA BLA BLA BLA BLA BLAB.
                        </div>
                    </section>
                    <p></p>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-4">
                <div class="form-group">
                    <label for="developer_name">Developer Name</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-university"></i>
                        </div>
                        <input type="text" maxlength="100" class="form-control flat no-shadow"  id="developer_name" name="developer_name" placeholder="Enter the name of developer...">
                    </div>
                </div>
            </div>
            <div class="col-xs-5">
                <div class="form-group">
                    <label for="developer_address">Address</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-building-o"></i>
                        </div>
                        <input type="text" maxlength="100" class="form-control flat no-shadow"  id="developer_address" name="developer_address" placeholder="Enter the address...">
                    </div>
                </div>
            </div>
            <div class="col-xs-3">
                <div class="form-group">
                    <label for="developer_postCode">Postcode</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-map-signs"></i>
                        </div>
                        <input type="text" maxlength="30" class="form-control flat no-shadow"  id="developer_postCode" name="developer_postCode" placeholder="Enter the postcode...">
                    </div>
                </div>
            </div>
        </div>


        <div class="row">
            <div class="col-xs-4">
                <div class="form-group">
                    <label for="developer_city">City</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-map-o"></i>
                        </div>
                        <input type="text" maxlength="100" class="form-control flat no-shadow"  id="developer_city" name="developer_city" placeholder="Enter the city...">
                    </div>
                </div>
            </div>
            <div class="col-xs-5">
                <div class="form-group">
                    <label for="developer_country">Country</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-globe"></i>
                        </div>
                        <input type="text" maxlength="50" class="form-control flat no-shadow"  id="developer_country" name="developer_country" placeholder="Enter the country...">
                    </div>
                </div>
            </div>
            <div class="col-xs-3">
                <div class="form-group">
                    <label for="developer_email">Email</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-envelope-o"></i>
                        </div>
                        <input type="text" maxlength="100" class="form-control flat no-shadow"  id="developer_email" name="developer_email" placeholder="Enter email...">
                    </div>
                </div>
            </div>
        </div>


        <div class="row">
            <div class="col-xs-9">
                <div class="form-group">
                    <label for="developer_website">Website</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="glyphicon glyphicon-link"></i>
                        </div>
                        <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="developer_website" name="developer_website" placeholder="Enter the link of website...">
                    </div>
                </div>
            </div>
            <div class="col-xs-3">
                <div class="form-group">
                    <label for="developer_company">Company</label>
                    <div class="input-group" >
                        <div class="input-group-addon">
                            <i class="fa fa-envelope-o"></i>
                        </div>
                        <input type="text" maxlength="50" class="form-control flat no-shadow" id="developer_company" name="developer_company" placeholder="Enter company...">
                    </div>
                </div>
            </div>
        </div>


        <div class="col-xs-12 no-margin">
            <div class="row" id="alertDeveloperMessage">
            </div>
        </div>


    </div>

    <div class="box-footer">
        <a  class="btn btn-primary flat" id="btnBack" onClick="window.location.href='./home.php';" style="width:100px"><b>Back</b></a>
        <button class="btn btn-success pull-right flat" id="btnPublishDeveloper" style="width:100px"><b>Next</b>
            <span class="fa fa-arrow-circle-right"></span>
        </button>
    </div>
</div>
</form>

<script src="./plugins/publish/developer.js"></script>