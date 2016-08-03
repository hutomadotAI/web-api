<?php
$msg ='<div class="row">';
$msg.='<div class="col-md-12">';
$msg ="<div class='box box-solid box-danger flat no-shadow'>";
$msg.="<div class='box-header with-border'>";
$msg.='<i class="fa fa-warning"></i>';
$msg.='<h3 class="box-title " >Alerts</h3>';
$msg.='</div>';
$msg.='<div class="box-body">';
$msg.='<div class="alert alert-warning alert-dismissable">';
$msg.='<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>';
$msg.='<h4><i class="icon fa fa-ban"></i> Alert!</h4>';



if (isset($_GET['err'])) {

    switch ($_GET['err']) {

        case 1 :
            $msg.='SESSION EXIPED';
            break;
        case 2 :
            $msg.='SESSION AI VARIABILES INCOMPLETE';
            break;
        case 3 :
            $msg.='DOMAINS GET INFO ERROR';
            break;
        case 4 :
            $msg.='RESPONSE JSON USER DOMAINS ERROR';
            break;
        case 5 :
            $msg.='RESPONSE JSON CREATE AI ERROR';
            break;
        case 6 :
            $msg.='NO CURRENT AI SET';
            break;
        case 7 :
            $msg.='OVERFLOW list of seleted AIs';
            break;
        case 8 :
            $msg.='JSON list of AIs error';
            break;
        case 9 :
            $msg.='JSON Delete AI ERROR';
            break;
        case 0 :
            $msg.='JSON chat ERROR';
            break;
        case 10 :
            $msg.='aiid SESSION MISSING';
            break;
        case 11 :
            $msg.='training FILE ERROR';
            break;
        default:
            $msg.='UNDEFINED ERROR';
    }

}
else
    $msg.='UNESPECTED ERROR OCCURED';

$msg.='</div>';
$msg.='</div>';
$msg.='</div>';
echo $msg;
?>
