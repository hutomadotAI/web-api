<?php
/**
 * For Development Purposes
 */
ini_set("display_errors", "on");
require __DIR__ . "/../class.logsys.php";
\hutoma\console::config(array(
    "db" => array(
        "host" => "10.132.0.9",
        "port" => 12306,
        "username" => "hutoma_caller",
        "password" => '>YR"khuN*.gF)V4#',
        "name" => "hutoma",
        "ai" => "ai",
        "table" => "users",
        "token_table" =>  "resetTokens"
    ),
    "api" => array(
        "request_url" => "http://10.132.0.6:11080/v1"
      //"request_url" => "http://api.hutoma.com/v1"
      //"request_url" => "http://localhost:8081/v1"
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
    ),
    "logging" => array(
      //"url" => "http://log-svc:18080/log"
    )
));

