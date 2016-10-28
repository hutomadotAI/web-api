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
    "api" => array(
        "request_url" => "https://api.hutoma.com/v1"
        //"request_url" => "http://localhost:8080/v1"
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


