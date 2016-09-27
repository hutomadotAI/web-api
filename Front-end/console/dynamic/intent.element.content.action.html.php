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


     <div class="box-body no-margin" id="boxAction" style="padding-top: 0;">
        
         <div class="row">
             <div class="col-md-12">
                 <h5 class="box-title">
                     <div class="inner-addon left-addon">
                         <i class="fa fa-wrench text-success"></i>
                         <input type="text" class="form-control" id="action-reaction" name="action-reaction" placeholder="Enter action name" style="padding-left: 35px;" onkeyup="actionReaction(this,event.keyCode)">
                     </div>
                 </h5>
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
                     need help? check our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                 </div>
             </div>
         </div>

         <div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px; padding-bottom:3px;">
             <div class="row">
                 <div class="col-xs-3 border-right">
                     <div class="text-center" >
                         <i class="fa fa fa-sitemap text-md text-md text-yellow" data-toggle="tooltip" title="This parametere needs to" ></i>
                         Entity
                     </div>
                 </div>
                 <div class="col-xs-4 border-right">
                     <div class="text-center" >
                         <i class="fa fa-sliders text-md text-red" data-toggle="tooltip" title="This parametere needs to" ></i>
                         Parameter
                     </div>
                 </div>
                 <div class="col-xs-3 border-right">
                     <div class="text-center" >
                         <i class="fa  fa-tag text-md text-blue" data-toggle="tooltip" title="This parametere needs to" ></i>
                         Value
                     </div>
                 </div>
                 <div class="col-xs-2">
                     <div class="text-center" >
                         Apply
                     </div>
                 </div>
            </div>
         </div>


         <div class="box-body bg-white flat no-padding" id="parameter-list"></div>

         <p></p>
         <button type="button" class="btn btn-primary flat pull-left" id="addParameter" value="">Add parameter</button>
         

     </div>

</div>

