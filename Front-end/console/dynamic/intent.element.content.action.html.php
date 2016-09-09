 <div class="box box-solid box-clean flat no-shadow" >
   
    <div class="box-header with-border">
        <!--<i class="fa fa-wrench text-success"></i>-->
        <h3 class="box-title">Action</h3>
        <a data-toggle="collapse"  href="#collapseActionInfo" tabindex="-1">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>


     <div class="box-body no-margin" id="boxAction" style="padding-top: 0px;">
        
         <div class="row">
             <div class="col-md-12">
                 <h5 class="box-title">
                     <div class="inner-addon left-addon">
                         <i class="fa fa-wrench text-success"></i>
                         <input type="text" class="form-control" id="action-reaction" name="action-reaction" placeholder="Enter action name" style="padding-left: 35px;">
                     </div>
                 </h5>
             </div>
         </div>

         <div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;">
             <div class="row">
                 <div class="col-xs-2">
                     <div class="text-center" >
                         Required
                         <i class="fa fa-question-circle text-md text-yellow" data-toggle="tooltip" title="This parameter needs to" ></i>
                     </div>
                 </div>
                 <div class="col-xs-4">
                     <div class="text-center" >
                         Parameter
                         <i class="fa fa-question-circle text-md text-yellow" data-toggle="tooltip" title="This parametere needs to" ></i>
                     </div>
                 </div>
                 <div class="col-xs-3">
                     <div class="text-center" >
                         Entity
                         <i class="fa fa-question-circle text-md text-yellow" data-toggle="tooltip" title="This parametere needs to" ></i>
                     </div>
                 </div>
                 <div class="col-xs-3">
                     <div class="text-center" >
                         Value
                         <i class="fa fa-question-circle text-md text-yellow" data-toggle="tooltip" title="This parametere needs to" ></i>
                     </div>
                 </div>
            </div>
         </div>

         <div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;">
             <div class="row">
                 <div class="col-xs-2">
                     <div class="text-center" >
                         <input type="checkbox" id="required">
                     </div>
                 </div>
                 <div class="col-xs-4">
                     <div class="text-center" >
                         <input type="text" class="form-control no-border" id="action-parameter" name="action-parameter" placeholder="parameter name" style="padding-left: 35px;">
                     </div>
                 </div>
                 <div class="col-xs-3">
                     <div class="box-tools pull-right" >

                         <div class="text-center" >
                         <input type="text" class="form-control no-border" id="action-parameter" placeholder="entity name" onkeyup="findEntityList(this)" style="padding-left: 35px;">
                         </div>

                         <ul class="dropdown-menu flat">
                             <li class="footer"><a href="#">  <i class="fa fa-bullhorn"></i>Deactive Voice</a></li>
                             <li class="footer"><a href="#">  <i class="fa fa-microphone-slash"></i>Mute Microphone</a></li>
                             <li class="footer"><a href="#">  <i class="fa fa-adjust"></i>Color Voice</a></li>
                         </ul>
                         <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1" >
                             <i class="fa fa-bars hidden" id="btnList" ></i>
                         </a>
                    </div>
                 </div>
                 <div class="col-xs-3">
                     <div class="dropdown">
                         <input type="text" name="search" class="span3 form-control" id="states" style="margin: 0" placeholder="Search here..." autocomplete="off" >
                     </div>
                 </div>

             </div>
         </div>



     </div>







     <div id="collapseActionInfo" class="panel-collapse collapse">
             <div class="box-body">
                 <div class="overlay center-block">
                     <section class="content bg-gray-light" >
                         <div class="box-body">
                             <dl class="dl-horizontal">
                                 <dt>Description Action Manipulation</dt>
                                 <dd>Before start training process, y.</dd>
                                 <dt>Euismod</dt>
                                 <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
                                 <dd>Donec id elit non mi porta gravida at eget metus.</dd>
                                 <dt>Malesuada porta</dt>
                                 <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
                                 <dt>Felis euismod semper eget lacinia</dt>
                                 <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
                             </dl>
                         </div>
                     </section>
                     <p></p>
                     need help? check our <a href='#''>video tutorial</a> or email us <a href='#'>hello@hutoma.com</a>
                 </div>
             </div>
     </div>


</div>

