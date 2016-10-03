 <div class="box box-solid box-clean flat no-shadow">
   
    <div class="box-header with-border ">
        <!--<i class="fa fa-comment-o text-green"></i>-->
        <h3 class="box-title"><?php echo $_POST['entity']?><span class="text-sm text-gray" style="padding: 0 3px 0 3px;"> > </span> Keys</h3>
        <a data-toggle="collapse"  href="#collapseKeysInfo">
            <div class=" pull-right">more info
                <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>
     

     <div class="box-body no-margin" id="boxKeys"  style="padding-top: 0;">
            <div class="row">
                <div class="col-md-12">
                    <h5 class="box-title">
                    <div class="inner-addon left-addon">
                        <i class="fa fa-language text-red"></i>
                        <input type="text" class="form-control" id="key-entity" name="key-entity" placeholder="add entity keys" onkeydown="checkKeyCode(this,event.keyCode)"  style="padding-left: 35px;">
                    </div>
                    </h5>
                </div>
            </div>

             <div id="collapseKeysInfo" class="panel-collapse collapse">
                 <div class="box-body">
                     <div class="overlay center-block">
                         <section class="content bg-gray-light" >
                             <div class="box-body">
                                 <dl class="dl-horizontal">
                                     <dt>Description Expression Manipulation</dt>
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
                         need help? check out our <a href='#'>video tutorial</a> or email us <a href='#'>hello@email.com</a>
                     </div>
                 </div>
             </div>

            <div class="row" id="entityKeys-list"></div>
    </div>

</div>