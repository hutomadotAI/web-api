/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

#------------------------------------------------------------------------------------
# Create Django database
CREATE DATABASE  IF NOT EXISTS `django` CHARACTER SET utf8;
USE `django`;

# Privileges for `django_caller`@`%`
GRANT USAGE ON *.* TO 'django_caller'@'%' IDENTIFIED BY PASSWORD '*43AB6D5047308CDDD3C9C7BF244A184EB22559E2';
GRANT CREATE, ALTER, EXECUTE, INSERT, SELECT, DELETE, UPDATE, REFERENCES, INDEX ON `django`.* TO 'django_caller'@'%';




USE `hutoma`;

# Privileges for `django_caller`@`%`
GRANT USAGE ON *.* TO 'django_caller'@'%' IDENTIFIED BY PASSWORD '*43AB6D5047308CDDD3C9C7BF244A184EB22559E2';
GRANT EXECUTE ON `hutoma`.* TO 'django_caller'@'%';
# Grant read-only DB access to django_caller
GRANT SELECT ON `hutoma`.`users` TO 'django_caller'@'%';
#-End-Django-----------------------------------------------------------------------------------

#------------------------------------------------------------------------------------
# Template publishing update
ALTER TABLE botStore ADD COLUMN `publishing_type` tinyint(1) NOT NULL DEFAULT 1 AFTER `publishing_state`;
CREATE INDEX `idx_botStore_publishing_state_publishing_type` on botStore (`publishing_state`, `publishing_type`);

DROP PROCEDURE IF EXISTS `publishBot`;
DELIMITER ;;
CREATE DEFINER=`botStoreWriter`@`127.0.0.1` PROCEDURE `publishBot`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50),
  IN `param_name` VARCHAR(50),
  IN `param_description` VARCHAR(1024),
  IN `param_longDescription` TEXT,
  IN `param_alertMessage` VARCHAR(150),
  IN `param_badge` VARCHAR(20),
  IN `param_price` DECIMAL,
  IN `param_sample` TEXT,
  IN `param_lastUpdate` DATETIME,
  IN `param_category` VARCHAR(50),
  IN `param_licenseType` VARCHAR(50),
  IN `param_privacyPolicy` TEXT,
  IN `param_classification` VARCHAR(50),
  IN `param_version` VARCHAR(25),
  IN `param_videoLink` VARCHAR(1800),
  IN `param_publishingState` TINYINT(1),
  IN `param_publishingType` TINYINT(1)
)
    NO SQL
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
      ROLLBACK;
      SELECT -1;
    END;

    INSERT INTO botStore
    (dev_id, aiid, name, description, long_description, alert_message, badge, price, sample, last_update, category,
     privacy_policy, classification, version, video_link, license_type, publishing_state, publishing_type)
    VALUES (param_devId, param_aiid, param_name, param_description, param_longDescription, param_alertMessage,
                         param_badge, param_price, param_sample, param_lastUpdate, param_category, param_privacyPolicy, param_classification,
            param_version, param_videoLink, param_licenseType, param_publishingState, param_publishingType);

    SELECT LAST_INSERT_ID();
  END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `getPublishedBots`;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getPublishedBots`(
  IN `param_publishing_type` TINYINT(1)
)
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE publishing_state = 2 AND publishing_type = param_publishing_type;
  END ;;
DELIMITER ;
#-End-publish-----------------------------------------------------------------------------------


CREATE TABLE IF NOT EXISTS `hutoma`.`botTemplate` (
  `botId` INT NOT NULL,
  `template` LONGTEXT NULL,
  UNIQUE INDEX `botId_UNIQUE` (`botId` ASC),
  PRIMARY KEY (`botId`),
  CONSTRAINT `fk_botTemplate_botId`
    FOREIGN KEY (`botId`)
    REFERENCES `hutoma`.`botStore` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

GRANT SELECT ON `hutoma`.`botTemplate` TO 'aiReader'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`botTemplate` TO 'aiWriter'@'127.0.0.1';

DROP PROCEDURE IF EXISTS `hutoma`.`addBotTemplate`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addBotTemplate`(
  IN `param_botId` INT(11),
  IN `param_template` TEXT
)
BEGIN
    INSERT INTO botTemplate (botId, template) VALUES (param_botId, param_template);
  END ;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `hutoma`.`getBotTemplate`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotTemplate`(
  IN `param_botId` INT(11)
)
BEGIN
    SELECT `template` FROM botTemplate WHERE `botId` = param_botId;
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


CREATE INDEX `idx_ai_status_training_status` ON `ai_status` (`training_status`);
ALTER TABLE `hutoma`.`memoryIntent` 
CHANGE COLUMN `variables` `variables` MEDIUMTEXT NOT NULL ;

DROP PROCEDURE `updateMemoryIntent`;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateMemoryIntent`(IN `param_name` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_chatId` VARCHAR(50),
                                                                            IN `param_variables` MEDIUMTEXT, IN `param_isFulFilled` TINYINT(1))
BEGIN
    INSERT INTO memoryIntent (aiid, chatId, name, variables, lastAccess, isFulfilled)
    VALUES(param_aiid, param_chatId, param_name, param_variables, NOW(), param_isFulFilled)

    ON DUPLICATE KEY UPDATE variables = param_variables, lastAccess = NOW(), isFulfilled = param_isFulFilled;
  END ;;
DELIMITER ;
