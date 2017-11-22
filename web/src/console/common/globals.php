<?php
// Report all errors
error_reporting(E_ALL);
// Don't display errors to users
ini_set("display_errors", "off");

date_default_timezone_set('Europe/London');


/**
 *
 * Disable XML entity loading.
 * Note: this will disable loading any external xml entities including
 * url loading in simplexml_load_file() and likely other libxml based functions that deal with URLs
 * as well as <xsl:import />
 */
if (function_exists('libxml_disable_entity_loader')) {
    libxml_disable_entity_loader(true);
}

/**
 * Make sure session variables are available throughout
 */
session_start();
?>