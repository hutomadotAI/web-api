<div class="box box-solid flat no-shadow drop-zone-580">
    <div class="box-body">
        <!-- row 1 -->
        <div class="row no-margin">
            <div class="col-xs-4 drop-zone">
                <div class="row no-margin">
                    <?php include './dynamic/input.image.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin">
                    <div class="form-group">
                        <label for="ai_description">Short Description</label>
                        <input type="text" class="form-control flat no-shadow"  id="ai_description" name="ai_description">
                    </div>
                    <div class="form-group">
                        <label for="ai_confidence">Long Description</label>
                        <textarea rows="6" class="form-control flat" value="" placeholder="Enter long description..." id="longDescription"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 1 -->
        <br>
        <!-- row 2 -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <div class="col-xs-4 drop-zone2">
                <div class="row no-margin">
                    <?php include './dynamic/input.licenceType.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.category.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.classification.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.price.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.version.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin" style="padding-top:10px;">
                    <div class="form-group no-margin">
                        <label for="ai_confidence">Show Example of Conversation</label>
                        <textarea rows="12" class="form-control flat" value="" placeholder="Add sample of conversation..." id="usecase"></textarea>
                    </div>
                    <div class="form-group" style="padding-top:15px;">
                        <label for="ai_link_privacy">Link Privacy Policy Page</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-eye"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_link_privacy" name="ai_link_privacy" placeholder="Enter a link to privacy policy">
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 2 -->
        <br>
        <!-- row 3 -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <div class="row no-margin">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label for="ai_developer_name">Developer Name</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-university"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_name" name="ai_developer_name" placeholder="Enter the name of developer...">
                        </div>
                    </div>
                </div>
                <div class="col-xs-5">
                    <div class="form-group">
                        <label for="ai_developer_address">Address</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-building-o"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_address" name="ai_developer_address" placeholder="Enter the address...">
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 no-padding">
                    <div class="form-group">
                        <label for="ai_developer_postcode">Postcode</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-map-signs"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_postcode" name="ai_developer_postcode" placeholder="Enter the postcode...">
                        </div>
                    </div>
                </div>
            </div>

            <div class="row no-margin">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label for="ai_developer_city">City</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-map-o"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_city" name="ai_developer_city" placeholder="Enter the city...">
                        </div>
                    </div>
                </div>
                <div class="col-xs-5">
                    <div class="form-group">
                        <label for="ai_developer_nation">Nation</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-globe"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_nation" name="ai_developer_nation" placeholder="Enter the nation...">
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 no-padding">
                    <div class="form-group">
                        <label for="ai_developer_email">Email</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-envelope-o"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_email" name="ai_developer_email" placeholder="Enter email...">
                        </div>
                    </div>
                </div>
            </div>
            <div class="row no-margin">
                <div class="col-xs-9" style="padding: 0px 15px 0px 0px">
                    <div class="form-group" style="padding: 0px 15px 0px 0px">
                        <label for="ai_developer_website">Website</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="glyphicon glyphicon-link"></i>
                            </div>
                            <input type="text" class="form-control flat no-shadow"  id="ai_developer_website" name="ai_developer_website" placeholder="Enter the link of website...">
                        </div>
                    </div>
                </div>
                <div class="col-xs-3" style="padding: 25px 0px 0px 0px; min-width: 140px;">
                    <div class="btn-group">
                        <button class="btn btn-success flat" data-dismiss="modal" id="btnPayment">
                            <b>cancel</b>
                        </button>
                    <button class="btn btn-success pull-right flat" data-dismiss="modal" id="btnPayment">
                        <b>Publish</b>
                        <span class="fa fa-arrow-circle-right"></span>
                    </button>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 3 -->
    </div>
</div>