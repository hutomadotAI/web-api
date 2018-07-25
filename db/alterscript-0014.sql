USE hutoma;

ALTER TABLE `intent` ADD COLUMN `intent_json` MEDIUMTEXT NULL DEFAULT NULL AFTER `created`;

DROP PROCEDURE `getIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `aiid`, `name`, `intent_json`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`aiid` = `in_aiid`;
END;;
DELIMITER ;


DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent_toDeprecate`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`, `last_updated`, `context_in`, `context_out`, `conditions_in`,
    `conditions_default_response`, `reset_context_on_exit`, `transitions`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`aiid` = `in_aiid`;
END;;
DELIMITER ;


DROP PROCEDURE `addUpdateIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250),
  IN in_new_name varchar(250),
  IN in_intent_json MEDIUMTEXT
)
BEGIN
    INSERT INTO `intent` (`aiid`, `name`, `intent_json`)
      SELECT `aiid`, `in_name`, `in_intent_json`
      FROM ai
      WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `name`=`in_new_name`,`intent_json` = `in_intent_json`;
  END;;
DELIMITER ;