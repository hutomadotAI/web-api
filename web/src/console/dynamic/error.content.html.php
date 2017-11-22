<?php
$msg = '';
$details = '';
$errorCode = '';

if (isset($_GET['err']) && isset($_GET['msg'])) {
    $errorCode = $_GET['err'];
    $details = $_GET['msg'];
} else {
    $errorCode = 500;
    $msg = 'Internal Error';
    $details = 'An unexpected error occurred.';
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

