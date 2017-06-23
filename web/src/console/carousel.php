<?php
require_once "api/apiBase.php";
require_once "api/aiApi.php";
require_once "api/botApi.php";
require_once "api/botstoreApi.php";
require_once "common/bot.php";
require_once "common/sessionObject.php";
require_once "common/developer.php";
require_once "common/botstoreItem.php";
require_once "common/botstoreListParam.php";


header('P3P: CP="CAO PSA OUR"');
session_start();

$categories = json_decode(CAROUSEL_CATEGORIES);
$MAX_BOTCARDS_LOADED_FOR_CAROUSEL = 10;

$botCategorizedItems =[];

if(isset($_GET['category']) && $_GET['category']!=''){
    global $categories;
    global $MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL;
    $categories = [$_GET['category']];
    $MAX_BOTCARDS_VISIBLE_FOR_CAROUSEL = '';
    $category = $_GET['category'];
}

$botstoreApi = new \hutoma\api\botstoreApi(false, \hutoma\sessionObject::getDevToken());
$botstoreListParam = new \hutoma\botstoreListParam();
$botstoreListParam->setPageSize($MAX_BOTCARDS_LOADED_FOR_CAROUSEL);


if (!isset($category) || empty($category)) {
    $botstoreItems = $botstoreApi->getBotstoreListPerCategory($botstoreListParam);
    foreach($botstoreItems['categories'] as $resultCat) {
        foreach($resultCat as $botstoreItem) {
            $botItem = \hutoma\botstoreItem::fromObject($botstoreItem);
            $botCategory = $botItem->getMetadata()['category'];
            $array = $botCategorizedItems[$botCategory];
            if (!isset($array)) {
                $array = [];
            }
            array_push($array, $botItem->toJSON());
            $botCategorizedItems[$botCategory] = $array;
        }
    }

} else {
    // Hack since 'featured' is not really a category
    if ($category == 'featured') {
        $botstoreListParam->addFilter('featured',1);
    } else {
        $botstoreListParam->addFilter('category', $category);
    }
    $botstoreItems = $botstoreApi->getBotstoreList($botstoreListParam);
    if (isset($botstoreItems) && (array_key_exists("items", $botstoreItems)) && sizeof($botstoreItems['items']) > 0 ) {
        $tmp_category_botItems = [];
        foreach ($botstoreItems['items'] as $botstoreItem) {
            $botItem = \hutoma\botstoreItem::fromObject($botstoreItem);
            $tmp_botItem = $botItem->toJSON();
            array_push($tmp_category_botItems, $tmp_botItem);
        }
        $botCategorizedItems[$category] = $tmp_category_botItems;
    }
}

echo json_encode($botCategorizedItems, true);
unset($botCategorizedItems);
unset($categories);


