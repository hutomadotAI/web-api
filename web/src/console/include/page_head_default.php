<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | <?php echo $header_page_title ?></title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="scripts/external/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="scripts/external/iCheck/all.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">

    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">

    <?php
    if (isset($header_additional_entries)) {
        echo $header_additional_entries;
    }
    ?>
    <script src="scripts/external/autopilot/autopilot.js"></script>
</head>