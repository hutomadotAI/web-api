USE hutoma;

ALTER TABLE `ai` MODIFY `default_chat_responses` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `ai` MODIFY `api_keys_desc` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `ai_integration` MODIFY `data` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `bot_ai_config` MODIFY `config` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;

DROP PROCEDURE IF EXISTS `updateAiIntegration`;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_data` LONGTEXT,
  IN `in_status` VARCHAR(1024),
  IN `in_active` TINYINT,
  IN `in_deactivate_message` VARCHAR(250))
BEGIN

IF NOT (NULLIF(`in_integrated_resource`, '') IS NULL) THEN
    UPDATE `ai_integration` SET
        `integrated_resource`='',
        `status`=`in_deactivate_message`,
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
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `setAiBotConfig`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setAiBotConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_botId` INT(11),
  IN `in_config` LONGTEXT)
    MODIFIES SQL DATA
BEGIN
  INSERT INTO `bot_ai_config` SET
    `dev_id` = `in_dev_id`,
    `aiid` = `in_aiid`,
    `botId` = `in_botId`,
    `config`= `in_config`
  ON DUPLICATE KEY UPDATE `config` = `in_config`;
END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `setBotConfigDefinition`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setBotConfigDefinition`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_config_def` LONGTEXT)
    MODIFIES SQL DATA
BEGIN
    UPDATE ai
    SET `api_keys_desc` = `in_config_def`
    WHERE aiid = in_aiid AND dev_id = in_dev_id;
END;;
DELIMITER ;