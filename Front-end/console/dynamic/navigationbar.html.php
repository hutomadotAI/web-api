<div class="navbar-custom-menu">
      <ul class="nav navbar-nav">

        <!-- User Account: style can be found in dropdown.less -->
        <li class="dropdown user user-menu">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown">
            <img src="./dist/img/user1-160x160.jpg" class="user-image" alt="User Image">
            <span class="hidden-xs">
            <?php echo $_SESSION['user_name'];?>
            </span>
          </a>
          <ul class="dropdown-menu">
            <!-- User image -->
            <li class="user-header">
              <img src="./dist/img/user1-160x160.jpg" class="img-circle" alt="User Image">
              <p>
                <?php echo $_SESSION['user_name'],' - planID ',$_SESSION['user_plan'];?>
                <small>
                  <?php echo 'joined since ',$_SESSION["user_joined"] ;?>
                </small>
              </p>
            </li>
            <!-- Menu Footer-->
            <li class="user-footer">
              <div class="pull-left">
                <a href="#" class="btn btn-default btn-flat">Profile</a>
              </div>
              <div class="pull-right">
                <a href="#" class="btn btn-default btn-flat">Sign out</a>
              </div>
            </li>
          </ul>
        </li>
        <!-- Control Sidebar Toggle Button -->
        <li>
          <a href="#" data-toggle="control-sidebar"><i class="fa fa-gears"></i></a>
        </li>
      </ul>
    </div>
</nav>


