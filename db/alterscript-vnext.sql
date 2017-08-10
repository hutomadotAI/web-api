/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

/*
Any bots that were set QUEUED but never actually queued due to RNN mis-reporting
will be set to TRAINING so that the API will treat them as failed and recover them
*/
UPDATE hutoma.ai_status 
SET training_status='ai_training'
WHERE training_status='ai_training_queued'
AND queue_time IS NULL;

DROP procedure IF EXISTS `getAi`;

DELIMITER $$
USE `hutoma`$$
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN

    SELECT
      `id`,
      `aiid`,
      `ai_name`,
      `ai_description`,
      `created_on`,
      `dev_id`,
      `is_private`,
      `client_token`,
      `hmac_secret`,
      `ui_ai_language`,
      `ui_ai_timezone`,
      `ui_ai_confidence`,
      `ui_ai_personality`,
      `ui_ai_voice`,
      `passthrough_url`,
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;

  END$$

DELIMITER ;

USE `hutoma`;
DROP procedure IF EXISTS `getAIs`;

DELIMITER $$
USE `hutoma`$$
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
    `ai`.`default_chat_responses`,
    `ai`.`passthrough_url`,
    (SELECT COUNT(`ai_training`.`aiid`)
     FROM `ai_training`
     WHERE `ai_training`.`aiid`=`ai`.`aiid`)
      AS `has_training_file`,
    `botStore`.`publishing_state`
  FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`deleted`=0;

  END$$

DELIMITER ;


ALTER TABLE `hutoma`.`ai` 
ADD COLUMN `passthrough_url` VARCHAR(2048) NULL DEFAULT NULL AFTER `deleted`;

