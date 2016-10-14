<?php
$msg = '';
$details = '';
$errorCode = '';

if (isset($_GET['err'])) {
    $errorCode = $_GET['err'];
    switch ($_GET['err']) {

        case 1 :
            $msg = 'SESSION_EXPIRED';
            $details = ' We could not find the data you were looking for';
            break;
        case 2 :
            $msg = 'POST AI VARIABLES INCOMPLETE';
            $details = ' We could not find the data you were looking for';
            break;
        case 3 :
            $msg = 'DOMAINS GET INFO ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 4 :
            $msg = 'RESPONSE JSON USER DOMAINS ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 6 :
            $msg = 'NO CURRENT AI SET';
            $details = ' We could not find the data you were looking for';
            break;
        case 7 :
            $msg = 'OVERFLOW list of selected AIs';
            $details = ' We could not find the data you were looking for';
            break;
        case 8 :
            $msg = 'JSON list of AIs error';
            $details = ' We could not find the data you were looking for';
            break;
        case 0 :
            $msg = 'JSON chat ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 10 :
            $msg = 'aiid SESSION MISSING';
            $details = ' We could not find the data you were looking for';
            break;
        case 11 :
            $msg = 'training FILE ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 12 :
            $msg = 'Payment Marketplace Data missing FILE ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 13 :
            $msg = 'Update AI status Failed!';
            $details = ' We could not find the data you were looking for';
            break;
        case 15 :
            $msg = 'RESPONSE JSON GET SINGLE AI INFO ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 16 :
            $msg = 'NEW INTENT NAME TRANSMISSION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 17 :
            $msg = 'NEW ENTITY NAME TRANSMISSION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 18 :
            $msg = 'AI TRAINING GET INF4O ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 100 :
            $msg = 'SESSION USER INFO FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 101 :
            $msg = 'SESSION EXPIRED';
            $details = ' We could not find the data you were looking for';
            break;
        case 102 :
            $msg = 'NAVIGATION ID MISSING';
            $details = ' We could not find the data you were looking for';
            break;
        case 103 :
            $msg = 'DOMAINS GET INFO ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 104 :
            $msg = 'INTEGRATION GET INFO ERROR';
            $details = ' We could not find the data you were looking for';
            break;
        case 110 :
            $msg = 'POSTING NEW AI INFO FOR UPLOAD FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 150 :
            $msg = 'COOKIES NOT ACTIVATED';
            $details = ' We could not find the data you were looking for';
            break;
        case 200 :
            $msg = 'GET INFO AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 201 :
            $msg = 'CREATE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 202 :
            $msg = 'UPDATE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 203 :
            $msg = 'DELETE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 301 :
            $msg = 'INTERNAL ERROR - CREATE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 302 :
            $msg = 'INTERNAL ERROR - GET MULTIPLE AIs INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 303 :
            $msg = 'INTERNAL ERROR - GET SINGLE AI INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 304 :
            $msg = 'INTERNAL ERROR - UPDATE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 305 :
            $msg = 'INTERNAL ERROR - DELETE AI FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 330 :
            $msg = 'INTERNAL ERROR - REQUEST CHATTING FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 350 :
            $msg = 'INTERNAL ERROR - UPLOAD FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 360 :
            $msg = 'INTERNAL ERROR - REQUEST DOMAINS INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 370 :
            $msg = 'INTERNAL ERROR - REQUEST INTEGRATION INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        default:
            $msg = 'UNDEFINED ERROR';
            $details = ' We could not find the data you were looking for';
    }
} else {
    if (isset($_GET['errObj'])) {
        $errorJson = json_decode($_GET['errObj']);
        $errorCode = $errorJson->{'status'}->{'code'};
        $msg = 'API Error';
        $details = $errorJson->{'status'}->{'info'};
    } else {
        $msg = 'UNEXPECTED ERROR OCCURRED';
        $details = 'Error not catchable';
    }
}
?>


<!-- Main content -->

<div class="error-page">
    <h2 class="headline text-yellow"> <?= $errorCode ?></h2>
    <div class="error-content">
        <h3><i class="fa fa-warning text-yellow"></i> <?php echo $msg;
            unset($msg); ?></h3>
        <p>
            <?php echo $details;
            unset($details); ?>
        </p>
        <form class="search-form">
            <div class="input-group">
                <input type="text" name="search" class="form-control" placeholder="Search">
                <div class="input-group-btn">
                    <button type="submit" name="submit" class="btn btn-warning btn-flat"><i class="fa fa-search"></i>
                    </button>
                </div>
            </div><!-- /.input-group -->
        </form>
    </div><!-- /.error-content -->
</div><!-- /.error-page -->

