<?php
/**
 * For Development Purposes
 */
ini_set("display_errors", "on");
require __DIR__ . "/../class.logsys.php";
\hutoma\console::config(array(
  "db" => array(
    "host" => "52.44.215.190",
    "port" => 3306,
    "username" => "hutoma_caller",
    "password" => '>YR"khuN*.gF)V4#',
    "name" => "hutoma",
    "ai" => "ai",
    "table" => "users",
    "token_table" =>  "resetTokens"
  ),
  "features" => array(
    "auto_init" => true
  ),
  "pages" => array(
    "no_login" => array(
      "/",
      "/Hutoma/Front-end/pages/reset.php",
      "/Hutoma/Front-end/pages/register.php"
    ),
    "login_page" => "/Hutoma/Front-end/pages/login.php",
    "home_page" => "/Hutoma/Front-end/console/home.php"
  )
));


