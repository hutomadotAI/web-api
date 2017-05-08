<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "api/botstoreApi.php";
require_once "common/bot.php";
require_once "common/developer.php";
require_once "common/botstoreItem.php";
require_once "common/botstoreListParam.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$categories = json_decode(CAROUSEL_CATEGORIES);
$MAX_BOTCARDS_LOADED_FOR_CAROUSEL = 10;

$botCategorizedItems =[];

if(isset($_GET['category']) && $_GET['category']!=''){
    global $categories;
    global $MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL;
    $categories = [$_GET['category']];
    $MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL = '';
}

foreach ($categories as $category) {
    $botstoreApi = new \hutoma\api\botstoreApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $botstoreListParam = new \hutoma\botstoreListParam();
    $botstoreListParam->setPageSize($MAX_BOTCARDS_LOADED_FOR_CAROUSEL);
    $botstoreListParam->addFilter('category',$category);
    $botstoreItems = $botstoreApi->getBotstoreList($botstoreListParam->getQueryParameter());

    if (isset($botstoreItems) && (array_key_exists("items", $botstoreItems)) && sizeof($botstoreItems['items']) > 0 ) {

        $tmp_category_botItems = [];
        foreach ($botstoreItems['items'] as $botstoreItem) {
            $botItem = \hutoma\botstoreItem::fromObject($botstoreItem);
            $tmp_botItem = $botItem->toJSON();
            array_push($tmp_category_botItems,$tmp_botItem);
        }
        $botCategorizedItems[$category] = $tmp_category_botItems;
    }
}
echo json_encode($botCategorizedItems, true);
unset($botCategorizedItems);
unset($categories);


