USE hutoma;

ALTER TABLE `intent_variable` ADD COLUMN `clear_on_entry` TINYINT(1) NOT NULL DEFAULT '0' AFTER `lifetime_turns`;


DROP PROCEDURE `getIntentVariables`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentVariables`(
  IN in_aiid VARCHAR(50),
  IN in_intent_name VARCHAR(250)
)
BEGIN

    SELECT
      `intent_variable`.`id` AS `id`,
      `entity`.`name` AS `entity_name`,
      `intent_variable`.`required` AS `required`,
      `intent_variable`.`n_prompts` AS `n_prompts`,
      `intent_variable`.`value` AS `value`,
      `entity`.`dev_id` AS `dev_id`,
      `entity`.`isPersistent` as `isPersistent`,
      `intent_variable`.`label` as `label`,
      `intent_variable`.`lifetime_turns` as `lifetime_turns`,
      `intent_variable`.`clear_on_entry` as `clear_on_entry`
    FROM `intent_variable`, `entity`
    WHERE `intent_variable`.`intent_id` =
          (SELECT `id` FROM `intent`
           WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid`)
          AND `entity`.`id` =
              (SELECT `id` FROM `entity`
               WHERE `id` = `intent_variable`.`entity_id`);

END;;
DELIMITER ;
