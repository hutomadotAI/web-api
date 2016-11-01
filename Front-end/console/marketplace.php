<?php
    require "../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }

    if (! isSessionVariablesAvailable() ) {
        \hutoma\console::redirect('./error.php?err=105');
        exit;
    }

    $domains= \hutoma\console::getBotsInStore();
    $AisMesh = \hutoma\console::getMesh($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);
    
    //TODO replace me with API call once done
    //if ($domains['status']['code'] !== 200) {
    if ($domains === '') {
        unset($domains);
        \hutoma\console::redirect('./error.php?err=103');
        exit;
    }
    //TODO replace me with more detail on value response
    if ($AisMesh['status']['code'] == 200){
        $AisMesh = $AisMesh['mesh'];
    }
    else{
        $AisMesh ="";
    }


    function isSessionVariablesAvailable(){
        return  (
            isset($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid'])
        );
    }
?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Marketplace </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./plugins/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showDomains('',1)">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <?php include './dynamic/marketplace.content.html.php'; ?>
            <?php include './dynamic/domainsNewAI.content.info.details.html.php'; ?>
        </section>
    </div>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/marketplace/marketplace.js"></script>
<script src="./plugins/domain/domain.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>


<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","marketplace",2,true,false]);
    </script>
</form>

<script>
    var domains = <?php  echo json_encode($domains); unset($domains)?>;
    var aismesh = <?php  echo json_encode($AisMesh); unset($AisMesh)?>;
    var userActived ={};

    for (var x in domains) {
        var found = false;
        var key = domains[x].aiid;

        for(var index in aismesh) {
            if (aismesh[index].aiid_mesh === domains[x].aiid) {
                found = true;
                break;
            }
        }

        if(found)
                userActived[key] = true;
            else
                userActived[key] = false;
    }

    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'domains_list';

    function searchDomain(str) { showDomains(str,1);}

    function getIndexOf(value){
        for(var i=0; i<x.lenght; i++){
            if(x[i].a == value)
                return i;
        }
    }
</script>
</body>
</html>
