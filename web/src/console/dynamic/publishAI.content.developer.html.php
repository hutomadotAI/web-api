<?php
namespace hutoma;

require_once __DIR__ . "/../common/Assets.php";
require_once __DIR__ . "/../dist/manifest.php";


$assets = new Assets($manifest);
?>

<input type="hidden" name="ai" value="<?php echo $aiToPublish['aiid'] ?>" id="aiToPublish">
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
                   This is your first bot. Before publishing this to our store we need to collect some developer details.
            </span>
        </div>

        <div id="collapseInfoDeveloper" class="panel-collapse collapse">
            <div class="box-body no-margin no-padding">
                <div class="overlay center-block">
                    <section class="content-info" >
                        <div class="box-body">
                            <dl class="dl-horizontal no-margin" style="text-align:justify">
                                This info is required so that we can create an ownership certificate. You cannot publish without completing this section.
                            </dl>
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
                <?php include __DIR__ . '/../dynamic/input.country.html.php'; ?>
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
                            <i class="fa fa-users"></i>
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
        <button class="btn btn-success pull-right flat" id="btnPublishDeveloper" style="width:100px"><b>Save</b>
        </button>
    </div>
</div>

<script src="<?php $assets->getAsset('publish/developer.js') ?>"></script>