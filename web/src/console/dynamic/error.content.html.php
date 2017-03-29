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
        case 105 :
            $msg = 'POSTING NEW AI INFO INCOMPLETE';
            $details = ' We could not find the data you were looking for';
            break;
        case 106 :
            $msg = 'POSTING NEW DOMAINS ACTIVATED FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 110 :
            $msg = 'POSTING NEW AI INFO FOR UPLOAD FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 118 :
            $msg = 'POSTING SELECTION INTENT FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 119 :
            $msg = 'POSTING SELECTION ENTITY FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 120 :
            $msg = 'SESSION VARIABLES MISSING';
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
        case 204 :
            $msg = 'GET INFO BOT FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 205 :
            $msg = 'GET INFO WEBHOOK FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 210 :
            $msg = 'GET INFO INTENTS FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 211 :
            $msg = 'GET INFO INTENT FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 220 :
            $msg = 'GET INFO ENTITIES FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 225 :
            $msg = 'GET INFO ENTITY VALUES FAILED';
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
        case 306 :
            $msg = 'INTERNAL ERROR - GET INFO TRAINING FILE FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 307 :
            $msg = 'INTERNAL ERROR - GET INFO AI STATUS FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 308 :
            $msg = 'INTERNAL ERROR - START TRAINING FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 309 :
            $msg = 'INTERNAL ERROR - STOP TRAINING FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 310 :
            $msg = 'INTERNAL ERROR - GET INTENTS INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 311 :
            $msg = 'INTERNAL ERROR - GET INTENT INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 315 :
            $msg = 'INTERNAL ERROR - GET INTENT EXPRESSIONS FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 316 :
            $msg = 'INTERNAL ERROR - GET INTENT VARIABLES FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 317 :
            $msg = 'INTERNAL ERROR - DELETE INTENT FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 320 :
            $msg = 'INTERNAL ERROR - GET ENTITIES INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 325 :
            $msg = 'INTERNAL ERROR - GET ENTITY VALUES INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 326 :
            $msg = 'INTERNAL ERROR - DELETE ENTITY FAILED';
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
        case 351 :
            $msg = 'INTERNAL ERROR - GET DEEP LEARNING ERROR INFO  FAILED';
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
        case 380 :
            $msg = 'INTERNAL ERROR - REQUEST DEVELOPER INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 381 :
            $msg = 'INTERNAL ERROR - UNABLE TO UPDATE DEVELOPER INFORMATION';
            $details = ' We could not find the data you were looking for';
            break;
        case 385 :
            $msg = 'INTERNAL ERROR - UNABLE TO UPDATE BOT INFORMATION';
            $details = ' We could not find the data you were looking for';
            break;
        case 386 :
            $msg = 'INTERNAL ERROR - UNABLE TO UPLOAD BOT ICON';
            $details = ' We could not find the data you were looking for';
            break;
        case 387 :
            $msg = 'INTERNAL ERROR - UNABLE TO GET BOT ICON';
            $details = ' We could not find the data you were looking for';
            break;
        case 390 :
            $msg = 'INTERNAL ERROR - UNABLE TO UPDATE BOT IMAGE';
            $details = ' We could not find the data you were looking for';
            break;
        case 391 :
            $msg = 'INTERNAL ERROR - REQUEST SINGLE BOT INFORMATION FAILED';
            $details = ' We could not find the data you were looking for';
            break;
        case 500 :
            $msg = 'INTERNAL ERROR - UNEXCEPTION ERROR';
            $details = ' We could not find the data you were looking for';
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

        <div class="input-group">
            <input type="text" name="search" class="form-control" placeholder="Search">
            <div class="input-group-btn">
                <button name="submit" class="btn btn-warning btn-flat"><i class="fa fa-search"></i>
                </button>
            </div>
        </div><!-- /.input-group -->

    </div><!-- /.error-content -->
</div><!-- /.error-page -->

