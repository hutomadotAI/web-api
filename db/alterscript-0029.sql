USE hutoma;

ALTER TABLE `hutoma`.`ai` ADD COLUMN `engine_version` VARCHAR(10) NOT NULL DEFAULT "default";

DROP PROCEDURE IF EXISTS `getAi`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
      `ai`.`id`,
      `ai`.`aiid`,
      `ai_name`,
      `ai_description`,
      `created_on`,
      `ai`.`dev_id`,
      `is_private`,
      `client_token`,
      `hmac_secret`,
      `ui_ai_language`,
      `ui_ai_timezone`,
      `ui_ai_confidence`,
      `ui_ai_personality`,
      `ui_ai_voice`,
      `passthrough_url`,
      `default_chat_responses`,
      `api_keys_desc`,
      `handover_reset_timeout`,
      `handover_message`,
      `error_threshold_handover`,
      `engine_version`,
      `botStore`.`publishing_state` as `publishing_state`,
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getAIs`;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIs`(
  IN `in_dev_id` VARCHAR(50))
    READS SQL DATA
BEGIN
  SELECT
    `ai`.`id`,
    `ai`.`aiid`,
    `ai`.`ai_name`,
    `ai`.`ai_description`,
    `ai`.`created_on`,
    `ai`.`dev_id`,
    `ai`.`is_private`,
    `ai`.`client_token`,
    `ai`.`hmac_secret`,
    `ai`.`ui_ai_language`,
    `ai`.`ui_ai_timezone`,
    `ai`.`ui_ai_confidence`,
    `ai`.`ui_ai_personality`,
    `ai`.`ui_ai_voice`,
    `ai`.`passthrough_url`,
    `ai`.`default_chat_responses`,
    `ai`.`api_keys_desc`,
    `ai`.`handover_reset_timeout`,
    `ai`.`handover_message`,
    `ai`.`error_threshold_handover`,
    `ai`.`engine_version`,
    (SELECT COUNT(`ai_training`.`aiid`)
     FROM `ai_training`
     WHERE `ai_training`.`aiid`=`ai`.`aiid`)
      AS `has_training_file`,
    `botStore`.`publishing_state`
  FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`deleted`=0;
  END;;
DELIMITER ;