<div class="col-xs-12">
    <div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;">
        <div class="row">


            <div class="col-xs-3" id="obj-entity">
                <input type="text" class="form-control no-border no-padding" name="entity-label" id="entity-label" placeholder="entity 1">
            </div>

            <div class="col-xs-6">
                <div class="box-body bg-white flat border" id="synonyms'+x+'" style="display:none;"  onkeydown="if(event.keyCode == 9 ){ tabEvent(); }">
                <input type="text" class="form-control no-border no-padding" name="synonym-label" id="synonym-label" placeholder="enter synonym" >
                </div>
            </div>
        </div>


        <div class="col-xs-3" id="btnEnt"  style="display:none;" >
            <div class="btn-group pull-right text-gray" style="padding-top: 5px;">


                <a data-toggle="control-sidebar"><i class="fa fa-object-group" style="padding-right: 5px;" data-toggle="tooltip" title="Define synonyms"></i></a>
                <a data-toggle="control-sidebar" ><i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i></a>
            </div>
        </div>

    </div>
</div>
</div>