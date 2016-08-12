
<?php
$msg ='';
if (isset($_GET['err'])) {
    switch ($_GET['err']) {

        case 1 :
            $msg ='SESSION EXIPED';
            break;
        case 2 :
            $msg ='SESSION AI VARIABILES INCOMPLETE';
            break;
        case 3 :
            $msg ='DOMAINS GET INFO ERROR';
            break;
        case 4 :
            $msg ='RESPONSE JSON USER DOMAINS ERROR';
            break;
        case 5 :
            $msg ='RESPONSE JSON CREATE AI ERROR';
            break;
        case 6 :
            $msg ='NO CURRENT AI SET';
            break;
        case 7 :
            $msg ='OVERFLOW list of seleted AIs';
            break;
        case 8 :
            $msg ='JSON list of AIs error';
            break;
        case 9 :
            $msg ='JSON Delete AI ERROR';
            break;
        case 0 :
            $msg ='JSON chat ERROR';
            break;
        case 10 :
            $msg ='aiid SESSION MISSING';
            break;
        case 11 :
            $msg ='training FILE ERROR';
            break;
        case 12 :
            $msg ='Payment Marketplace Data missing FILE ERROR';
            break;
        default:
            $msg ='UNDEFINED ERROR';
    }
}
else
    $msg ='UNESPECTED ERROR OCCURED';
?>

<div class="row">
    <div class="col-md-12">
        <div class='box box-solid box-clean flat no-shadow'>
            <div class='box-header with-border'>
                <i class="fa fa-warning text-danger"></i>
                <h3 class="box-title " >Alerts</h3>
            </div>
            <div class="box-body">
                <div class="alert alert-warning alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                    <h4><i class="icon fa fa-ban"></i> Alert!</h4>
                    <?php echo $msg; unset($msg); ?>
                </div>
            </div>
        </div>
    </div>
</div>