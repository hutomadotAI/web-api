/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `ai` ADD COLUMN hmac_secret varchar(50)  DEFAULT NULL AFTER `client_token`;
ALTER TABLE `users` DROP COLUMN `client_token`;


DROP PROCEDURE IF EXISTS `getWebhookSecretForBot`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getWebhookSecretForBot`(IN `param_aiid` VARCHAR(50))
BEGIN
    SELECT `hmac_secret` FROM `ai` WHERE `aiid` = `param_aiid`;
END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `setWebhookSecretForBot`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setWebhookSecretForBot`(
  IN `in_aiid` VARCHAR(50),
  IN `in_hmac_secret` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `hmac_secret` = `in_hmac_secret`
		WHERE `aiid`=`in_aiid`;
END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAi`;
DELIMITER ;;
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
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;

  END ;;
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
    (SELECT COUNT(`ai_training`.`aiid`)
     FROM `ai_training`
     WHERE `ai_training`.`aiid`=`ai`.`aiid`)
      AS `has_training_file`,
    `botStore`.`publishing_state`
  FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`deleted`=0;

  END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `addUser`;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUser`(IN `username` VARCHAR(50), IN `email` TINYTEXT, IN `password` VARCHAR(64), IN `password_salt` VARCHAR(250), IN `first_name` VARCHAR(30), IN `last_name` VARCHAR(30), IN `dev_token` VARCHAR(250), IN `plan_id` INT, IN `dev_id` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    INSERT INTO `users`(`username`, `email`, `password`, `password_salt`, `first_name`, `last_name`, `dev_token`, `plan_id`, `dev_id`)
    VALUES (username, email, password,password_salt, first_name,last_name, dev_token,plan_id, dev_id);
  END;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `updateAiIntegration`;
DELIMITER $$
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_data` JSON,
  IN `in_status` VARCHAR(1024),
  IN `in_active` TINYINT)
BEGIN

IF NOT (NULLIF(`in_integrated_resource`, '') IS NULL) THEN
	UPDATE `ai_integration` SET 
		`integrated_resource`='',
		`active`=0
        WHERE `ai_integration`.`integration`=`in_integration`
        AND `ai_integration`.`aiid`!=`in_aiid`
        AND `in_integrated_resource` = `ai_integration`.`integrated_resource`;
END IF;

INSERT INTO `ai_integration`
  (`aiid`, `integration`, 
  `integrated_resource`, `integrated_userid`,
  `data`,`status`, `active`,
  `update_time`)
(SELECT
  `in_aiid`, `in_integration`,
  `in_integrated_resource`,
  `in_integrated_userid`,
  `in_data`, `in_status`, `in_active`,
  now() 
FROM `ai`
WHERE `ai`.`aiid` = `in_aiid` AND `ai`.`dev_id` = `in_devid`)
ON DUPLICATE KEY UPDATE
	`integrated_resource` = `in_integrated_resource`,
    `integrated_userid` = `in_integrated_userid`,
	`data`=`in_data`,
    `status`=`in_status`,
    `active`=`in_active`,
    `update_time`=now();
  END$$
DELIMITER ;
