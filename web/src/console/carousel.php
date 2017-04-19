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

$CAROUSEL_CATEGORIES = ["Entertainment","Other","Education","Events", "Finance", "Fitness", "Games", "Health & Beauty", "Internet of Things", "News", "Personal", "Shopping", "Social", "Travel", "Virtual Assistants"];
$MAX_BOTCARDS_VISIBLE = 6;

$botCategorizedItems =[];

if(isset($_GET['category'])){
    global $CAROUSEL_CATEGORIES;
    global $MAX_BOTCARDS_VISIBLE;
    $CAROUSEL_CATEGORIES = [$_GET['category']];
    $MAX_BOTCARDS_VISIBLE = '';
}

foreach ($CAROUSEL_CATEGORIES as $category) {
    $botstoreApi = new \hutoma\api\botstoreApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $botstoreListParam = new \hutoma\botstoreListParam();
    $botstoreListParam->setPageSize($MAX_BOTCARDS_VISIBLE);
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


