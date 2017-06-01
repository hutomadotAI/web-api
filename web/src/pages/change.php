<?php

require_once "../console/common/utils.php";
require_once "../console/common/config.php";

require "config.php";
?>
<!DOCTYPE html>
<html>
  <head>
    <title>Change Password</title>
  </head>
  <body>
  <?php include_once "../google_analytics.php"; ?>

  <?php
    if(isset($_POST['change_password'])){
      if(isset($_POST['current_password']) && $_POST['current_password'] != "" && isset($_POST['new_password']) && $_POST['new_password'] != "" && isset($_POST['retype_password']) && $_POST['retype_password'] != "" && isset($_POST['current_password']) && $_POST['current_password'] != ""){
          
        $curpass = $_POST['current_password'];
        $new_password = $_POST['new_password'];
        $retype_password = $_POST['retype_password'];

          $api = new \hutoma\api\adminApi(\hutoma\console::isLoggedIn(), \hutoma\config::getAdminToken());
          $userInfo = $api->getUserInfo(\hutoma\console::$user);
          unset($api);

        if($new_password != $retype_password){
          echo "<p><h2>Password don't match</h2><p>The passwords you entered don't match. Try again.</p></p>";
        }else if(\hutoma\console::login($userInfo['username'], "", false, false) == false){
          echo "<h2>Incorrect Password</h2><p>The password you entered for your account is wrong.</p>";
        }else{
          $change_password = \hutoma\console::changePassword($new_password);
          if($change_password === true){
            echo "<h2>Password Changed Successfully</h2>";
          }
        }
      }else{
        echo "<p><h2>Password field was blank</h2><p>Form fields were left blank</p></p>";
      }
    }
    ?>
    <form action="<?php echo \hutoma\utils::curPageURL();?>" method='POST'>
      <label>
        <p>Current Password</p>
        <input type='password' name='current_password' />
      </label>
      <label>
        <p>New Password</p>
        <input type='password' name='new_password' />
      </label>
      <label>
        <p>Retype New Password</p>
        <input type='password' name='retype_password' />
      </label>
      <button style="display: block;margin-top: 10px;" name='change_password' type='submit'>Change Password</button>
    </form>
  </body>
</html>
