USE `hutoma`;

DROP PROCEDURE `addUpdateIntentVariable`;

ALTER TABLE `intent_variable` ADD COLUMN `lifetime_turns` INT(11) NOT NULL DEFAULT -1 AFTER `label`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntentVariable`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_name VARCHAR(250),
  IN in_entity_name VARCHAR(250),
  IN in_required int(1),
  IN in_n_prompts int,
  IN in_value varchar(250),
  IN in_label varchar(50),
  IN in_lifetime_turns INT(11)
)
BEGIN
    DECLARE update_count INT;

    INSERT INTO `intent_variable` (`intent_id`, `entity_id`, `required`, `n_prompts`, `value`, `label`, `lifetime_turns`)
      SELECT `intent`.`id`, `entity`.`id`, `in_required`, `in_n_prompts`, `in_value`, `in_label`, `in_lifetime_turns`
      FROM `intent`, `entity`
      WHERE `intent`.`id` =
            (SELECT `id` FROM `intent`
        WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
          (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`))
            AND `entity`.`id` =
                (SELECT `id` FROM `entity` WHERE
          (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1)
          AND `in_entity_name`=`name`)
    ON DUPLICATE KEY UPDATE
      `required`=`in_required`, `n_prompts`=`in_n_prompts`, `value`=`in_value`, `label`=`in_label`, `lifetime_turns`=`in_lifetime_turns`,
      `entity_id`= (SELECT `id` FROM `entity` WHERE (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1) AND `in_entity_name`=`name`),
      `dummy` = NOT `dummy`,
      `id` = LAST_INSERT_ID(`intent_variable`.`id`);

    SET update_count = row_count();

    SELECT update_count AS `update`, IF (update_count>0, last_insert_id(), -1) AS `affected_id`;

  END ;;
DELIMITER ;


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
      `intent_variable`.`lifetime_turns` as `lifetime_turns`
    FROM `intent_variable`, `entity`
    WHERE `intent_variable`.`intent_id` =
          (SELECT `id` FROM `intent`
           WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid`)
          AND `entity`.`id` =
              (SELECT `id` FROM `entity`
               WHERE `id` = `intent_variable`.`entity_id`);

END ;;
DELIMITER ;