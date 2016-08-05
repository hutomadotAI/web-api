<?php
/**
 * For Development Purposes
 */
ini_set("display_errors", "on");

require __DIR__ . "/../class.logsys.php";
\hutoma\console::config(array(
  "db" => array(
    "host" => "54.83.145.18",
    "port" => 3306,
    "username" => "root",
    "password" => 'P7gj3fLKtPhjU7aw',
    "name" => "hutoma",
    "ai" => "ai",
    "table" => "users"
  ),
  "features" => array(
    "auto_init" => true
  ),
  "pages" => array(
    "no_login" => array(
      "/",
      "/pages/reset.php",
      "/pages/register.php"
    ),
    "login_page" => "/pages/login.php",
    "home_page" => "/console/home.php"
  )
));