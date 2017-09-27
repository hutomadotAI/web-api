<?php

namespace hutoma;

use hutoma\api\userMgmt;

require_once __DIR__ . "/../console/common/globals.php";
require_once __DIR__ . "/../console/common/utils.php";
require_once __DIR__ . "/../console/common/config.php";
require_once __DIR__ . "/../console/api/userMgmt.php";

?>
<!DOCTYPE html>
<html>
  <head>
    <title>Hu:toma | Change Password</title>
    <link rel="icon" href="/console/dist/img/favicon.ico" type="image/x-icon">
    <?php include_once "../console/common/google_tag_manager.php" ?>
  </head>
  <body>
  <?php include_once __DIR__ . "/../console/common/google_tag_manager_no_js.php" ?>

  <?php
    if(isset($_POST['change_password'])){
      if(isset($_POST['current_password']) && $_POST['current_password'] != "" && isset($_POST['new_password']) && $_POST['new_password'] != "" && isset($_POST['retype_password']) && $_POST['retype_password'] != "" && isset($_POST['current_password']) && $_POST['current_password'] != ""){
          
        $curpass = $_POST['current_password'];
        $new_password = $_POST['new_password'];
        $retype_password = $_POST['retype_password'];

          $api = new api\adminApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
          $userInfo = $api->getUserInfo(sessionObject::getCurrentUsername());
          unset($api);

        if($new_password != $retype_password){
          echo "<p><h2>Password don't match</h2><p>The passwords you entered don't match. Try again.</p></p>";
        }else if(userMgmt::login($userInfo['username'], "", false, false) == false){
          echo "<h2>Incorrect Password</h2><p>The password you entered for your account is wrong.</p>";
        }else{
          $change_password = userMgmt::changePassword($new_password);
          if($change_password === true){
            echo "<h2>Password Changed Successfully</h2>";
          }
        }
      }else{
        echo "<p><h2>Password field was blank</h2><p>Form fields were left blank</p></p>";
      }
    }
    ?>
    <form action="<?php echo utils::curPageURL();?>" method='POST'>
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
