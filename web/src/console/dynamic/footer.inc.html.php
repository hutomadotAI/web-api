<div class="unselectable" id="footer">
  <div class="pull-right hidden-xs"></div>
  <strong>Copyright &copy; <?php echo date("Y"); ?>
    <a href="http://hutoma.com" tabindex="-1">Hu:toma</a>.</strong> All rights reserved.
</div>
<?php
$intercomAppId  = \hutoma\config::getIntercomAppId();

if(\hutoma\console::isLoggedIn()) {
    $userDetails = $_SESSION[$_SESSION['navigation_id']]['user_details'];
    if ($intercomAppId != null) {?>
        <script>
  window.intercomSettings = { app_id: '<?php echo $intercomAppId;?>' };
  (function(){var w=window;var ic=w.Intercom;if(typeof ic==="function"){ic('reattach_activator');ic('update',intercomSettings);}else{var d=document;var i=function(){i.c(arguments)};i.q=[];i.c=function(args){i.q.push(args)};w.Intercom=i;function l(){var s=d.createElement('script');s.type='text/javascript';s.async=true;s.src='https://widget.intercom.io/widget/<?php echo $app_id;?>';var x=d.getElementsByTagName('script')[0];x.parentNode.insertBefore(s,x);}if(w.attachEvent){w.attachEvent('onload',l);}else{w.addEventListener('load',l,false);}}})()
  window.Intercom('boot', {
    app_id: '<?php echo $app_id;?>',
    email: '<?php echo $userDetails['email'];?>',
    name: '<?php echo $userDetails['name'];?>',
    user_id: '<?php echo $userDetails['id'];?>',
    created_at: <?php echo strtotime($userDetails['created']);?>

  });
    </script>
  <?php } } ?>
