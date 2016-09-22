
<?php
$msg ='';
$details ='';
if (isset($_GET['err'])) {
    switch ($_GET['err']) {

        case 1 :
            $msg ='SESSION EXIPED';
            $details =' We could not find the data you were looking for';
            break;
        case 2 :
            $msg ='POST AI VARIABILES INCOMPLETE';
            $details =' We could not find the data you were looking for';
            break;
        case 3 :
            $msg ='DOMAINS GET INFO ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 4 :
            $msg ='RESPONSE JSON USER DOMAINS ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 5 :
            $msg ='RESPONSE JSON CREATE AI ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 6 :
            $msg ='NO CURRENT AI SET';
            $details =' We could not find the data you were looking for';
            break;
        case 7 :
            $msg ='OVERFLOW list of seleted AIs';
            $details =' We could not find the data you were looking for';
            break;
        case 8 :
            $msg ='JSON list of AIs error';
            $details =' We could not find the data you were looking for';
            break;
        case 9 :
            $msg ='JSON Delete AI ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 0 :
            $msg ='JSON chat ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 10 :
            $msg ='aiid SESSION MISSING';
            $details =' We could not find the data you were looking for';
            break;
        case 11 :
            $msg ='training FILE ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 12 :
            $msg ='Payment Marketplace Data missing FILE ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 13 :
            $msg ='Update AI status Failed!';
            $details =' We could not find the data you were looking for';
            break;
        case 15 :
            $msg ='RESPONSE JSON GET SINGLE AI INFO ERROR';
            $details =' We could not find the data you were looking for';
            break;
        case 16 :
            $msg ='NEW INTENT NAME TRASMISSION FAILED';
            $details =' We could not find the data you were looking for';
            break;
        case 17 :
            $msg ='NEW ENTITY NAME TRASMISSION FAILED';
            $details =' We could not find the data you were looking for';
            break;
        case 18 :
            $msg ='AI TRAINING GET INFO ERROR';
            $details =' We could not find the data you were looking for';
            break;
        default:
            $msg ='UNDEFINED ERROR';
            $details =' We could not find the data you were looking for';
    }
}
else {
    $msg = 'UNESPECTED ERROR OCCURED';
    $details = 'Error not catchable';
}
?>



<!-- Main content -->

    <div class="error-page">
        <h2 class="headline text-yellow"> <?php echo $_GET['err'] ?></h2>
        <div class="error-content">
            <h3><i class="fa fa-warning text-yellow"></i> <?php echo $msg; unset($msg); ?></h3>
            <p>
                <?php echo $details; unset($details); ?>
            </p>
            <form class="search-form">
                <div class="input-group">
                    <input type="text" name="search" class="form-control" placeholder="Search">
                    <div class="input-group-btn">
                        <button type="submit" name="submit" class="btn btn-warning btn-flat"><i class="fa fa-search"></i></button>
                    </div>
                </div><!-- /.input-group -->
            </form>
        </div><!-- /.error-content -->
    </div><!-- /.error-page -->

