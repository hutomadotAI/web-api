<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | <?php echo $header_page_title ?></title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="/console/dist/vendors/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="/console/dist/vendors/iCheck/all.css">
    <link rel="stylesheet" href="/console/dist/vendors/select2/select2.css">

    <link rel="stylesheet" href="/console/dist/vendors/bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="/console/dist/vendors/bootstrap/css/bootstrap.min.css">

    <link rel="stylesheet" href="/console/dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="/console/dist/css/hutoma.css">
    <link rel="stylesheet" href="/console/dist/css/skins/skin-blue.css">

    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">

    <?php
    if (isset($header_additional_entries)) {
        echo $header_additional_entries;
    }

    include_once __DIR__ . "/../common/google_tag_manager.php";
    ?>
</head>