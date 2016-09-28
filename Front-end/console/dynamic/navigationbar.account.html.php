<?php

  $username = 'unknown';
  $plan = 'not defined';
  $joined ='not available';

  try {
    if (isset($_SESSION[$_SESSION['navigation_id']]['user_details']['username']))
      $username = $_SESSION[$_SESSION['navigation_id']]['user_details']['username'];
    if (isset($_SESSION[$_SESSION['navigation_id']]['user_plan']))
      $plan = $_SESSION[$_SESSION['navigation_id']]['user_plan'];
    if (isset($_SESSION[$_SESSION['navigation_id']]['user_details']["user_joined"]))
      $joined = $_SESSION[$_SESSION['navigation_id']]['user_details']["user_joined"];
  }catch(Exception $e){

  }
?>

<div class="navbar-custom-menu">
      <ul class="nav navbar-nav">
        <!--
        <li>
          <a href="#" tabindex="-1">
            <i class="fa fa-shopping-cart text-green" style="position: relative;"></i>
            <span>Marketplace</span>
          </a>
        </li>
        -->

        <!-- User Account: style can be found in dropdown.less -->
        <li class="dropdown user user-menu">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" tabindex="-1" >
            <i class="fa fa-user text-blue" tabindex="-1"></i><span>Account</span>
          </a>

          <ul class="dropdown-menu">
            <!-- User image -->
            <li class="user-header">
              <img src="./dist/img/user1-160x160.jpg" class="img-circle" alt="User Image">
              <p>
                  <?php  echo ( $username.' - planID '. $plan);?>
                <small>
                  <?php echo 'joined since ',$joined ;?>
                </small>
              </p>
            </li>
            <!-- Menu Footer-->
            <li class="user-footer">
              <div class="pull-left">
                <a href="#" class="btn btn-default btn-flat" tabindex="-1">Profile</a>
              </div>
              <div class="pull-right">
                <a href="#" class="btn btn-default btn-flat" tabindex="-1">Sign out</a>
              </div>
            </li>
          </ul>
        </li>
      </ul>
</div>
<?php unset($username); unset($plan); unset($joined); ?>



