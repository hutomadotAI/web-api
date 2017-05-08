<?php
// Disable all PHP error reporting
error_reporting(0);
//ini_set("display_errors", "on"); // for development only

define('CAROUSEL_CATEGORIES',  json_encode(array("Entertainment", "Education","Events", "Finance", "Fitness", "Games", "Health & Beauty", "Internet of Things", "News", "Personal", "Other", "Shopping", "Social", "Travel", "Virtual Assistants")));
define('CAROUSEL_CATEGORIES_ICONS',  json_encode(array("fa-film", "fa-graduation-cap","fa-calendar-check-o", "fa-eur", "fa-bicycle", "fa-gamepad", "fa-heartbeat", "fa-laptop", "fa-newspaper-o", "fa-male", "fa-search", "fa-cart-plus", "fa-thumbs-o-up", "fa-plane", "fa-headphones")));

require __DIR__ . "/../class.logsys.php";
\hutoma\console::config(array(
    "db" => array(
        "host" => "10.132.0.4",
        "port" => 13306,
        "username" => "hutoma_caller",
        "password" => '>YR"khuN*.gF)V4#',
        "name" => "hutoma",
        "ai" => "ai",
        "table" => "users",
        "token_table" =>  "resetTokens"
    ),
    "api" => array(
        "request_url" => "http://10.132.0.4:8081/v1"
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