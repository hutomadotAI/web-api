<div class="box box-solid box-success flat no-shadow" id="newAicontent">
    <div class="box-header with-border">
        <h3 class="box-title">Settings Payment</h3>
    </div>
    <form method="POST" id="settingsPaymentform" action="./training.php">
        <div class="box-body">
            <div class="row">



                <!-- INPUT Name -->
                <div class="col-md-6">
                    <div class="form-group">
                        <a data-toggle="modal"  data-target="#infoPlans" style="cursor: pointer;">
                            <div class="pull-right" style="margin-top: 10px;">more info
                                <i class="fa fa-question-circle text-md text-yellow"></i>
                            </div>
                        </a>
                        <label>Choose your plan</label>
                        <select class="form-control select2" name="ai_language" id="ai_language"style="width: 100%;">
                            <option>Free</option>
                            <option selected="selected">Developer BASIC</option>
                            <option>Developer PRO</option>
                            <option>Developer ULTIMATE</option>
                            <option>Enterprise</option>
                        </select>
                    </div>
                </div>

                <!-- INPUT Language -->
                <div class="col-md-6">
                    <div class="form-group">
                        <a data-toggle="modal"  data-target="#infoContract" style="cursor: pointer;">
                            <div class="pull-right" style="margin-top: 10px;">more info
                                <i class="fa fa-question-circle text-md text-yellow"></i>
                            </div>
                        </a>
                        <label>Type of Payments</label>
                        <select class="form-control select2" name="ai_language" id="ai_language"style="width: 100%;">
                            <option>trial</option>
                            <option personal="selected">personal</option>
                            <option>Monthly fee</option>
                            <option>Daily traffic limit</option>
                            <option>Years</option>
                        </select>
                    </div>
                </div>
            </div>




        </div>
    </form>

    <div class="box-footer">
        <a href="newAI.php" class="btn btn-primary flat" id="btnCancel">cancel</a>
        <button type="submit" id="btnNext"  class="btn btn-success flat disabled" onClick="" alt="next step">next</button>
    </div>
</div>


<section>

    <div class="text-md text-center center-block " style="margin-top:20px;margin-bottom:10px;">Compare Plans</div>

    <div class="box box-solid box-success flat no-shadow">

            <div class="box-body with-border no-padding">
                <table class="table table-hover">
                    <tr>
                        <th></th>
                        <th class="text-center bg-gray-ultralight">Free</th>
                        <th class="text-center">Basic</th>
                        <th class="text-center bg-gray-ultralight">Pro</th>
                        <th class="text-center">Ultimate</th>
                        <th class="text-center bg-gray-ultralight">Enterprise</th>
                    </tr>
                    <tr>
                        <td>Monthly fee</td>
                        <td class="text-center bg-gray-ultralight">$0</td>
                        <td class="text-center">$99/mo</td>
                        <td class="text-center bg-gray-ultralight">$249/mo</td>
                        <td class="text-center">$499/mo</td>
                        <td class="text-center bg-gray-ultralight">contact us</td>
                    </tr>
                    <tr>
                        <td>Daily API Calls</td>
                        <td class="text-center bg-gray-ultralight">100 hits per day</td>
                        <td class="text-center">500 hits per day</td>
                        <td class="text-center bg-gray-ultralight">2000 hits per day</td>
                        <td class="text-center">5000 hits per day</td>
                        <td class="text-center bg-gray-ultralight">no limits</td>
                    </tr>

                    <tr>
                        <td>Number of AIs</td>
                        <td class="text-center bg-gray-ultralight">1</td>
                        <td class="text-center">1</td>
                        <td class="text-center bg-gray-ultralight">3</td>
                        <td class="text-center">5</td>
                        <td class="text-center bg-gray-ultralight">25</td>
                    </tr>

                    <tr>
                        <td>CPU time for AI Training</td>
                        <td class="text-center bg-gray-ultralight">1 Hour per month</td>
                        <td class="text-center">24 Hours per month</td>
                        <td class="text-center bg-gray-ultralight">200 Hours per month</td>
                        <td class="text-center">400 Hours per month</td>
                        <td class="text-center bg-gray-ultralight">no limits</td>
                    </tr>

                    <tr>
                        <td>Neural Network Training</td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Pattern based neurons</td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Semantic Analysis</td>
                        <td class="bg-gray-ultralight"></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Core Personality</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Learning on the Fly</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>


                    <tr>
                        <td>Speech support</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="bg-gray-ultralight"></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Email support</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="bg-gray-ultralight"></td>
                        <td class="text-center"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Phone support</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>

                    <tr>
                        <td>Samantha Neutal Network</td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="bg-gray-ultralight"></td>
                        <td></td>
                        <td class="text-center bg-gray-ultralight"><i class=" fa fa-check-circle-o text-md text-success" id="iconFile"></i></td>
                    </tr>



                </table>
            </div><!-- /.box-body -->

</section>

<!-- Modal INFO Type of plans-->
<div class="modal fade" id="infoPlans" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Type of plans</h4>
            </div>
            <div class="modal-body">
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description Actions</dt>
                        <dd>Tell the AI learning ...</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>


<!-- Modal INFO Type of contract-->
<div class="modal fade" id="infoContract" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Type of COntract</h4>
            </div>
            <div class="modal-body">
                <div class="box-body">
                    <dl class="dl-horizontal">
                        <dt>Description Actions</dt>
                        <dd>Tell the AI learning ...</dd>
                        <dt>Euismod</dt>
                        <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                        <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                        <dt>Malesuada porta</dt>
                        <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                        <dt>Felis euismod semper eget lacinia</dt>
                        <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                    </dl>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flat" data-dismiss="modal">Close</button>
            </div>
        </div>

    </div>
</div>
