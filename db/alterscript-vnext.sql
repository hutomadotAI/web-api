/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/


USE `hutoma`;

ALTER TABLE `intent` ADD COLUMN `last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `topic_out`;

ALTER TABLE `ai`
  ADD COLUMN `handover_message` varchar(2048) DEFAULT NULL AFTER `api_keys_desc`,
  ADD COLUMN `handover_reset_timeout` int(11) NOT NULL DEFAULT -1 AFTER `handover_message`,
  ADD COLUMN `error_threshold_handover` int(11) NOT NULL DEFAULT -1 AFTER `handover_reset_timeout`;
  
ALTER TABLE `chatState`
  ADD COLUMN `handover_reset` timestamp NULL DEFAULT NULL AFTER `chat_target`,
  ADD COLUMN `bad_answers_count` int(11) NOT NULL DEFAULT 0 AFTER `handover_reset`;


ALTER TABLE `users`
  DROP COLUMN `username`,
  DROP COLUMN `email`,
  DROP COLUMN `password`,
  DROP COLUMN `password_salt`,
  DROP COLUMN `first_name`,
  DROP COLUMN `attempt`,
  DROP COLUMN `last_name`;


DROP PROCEDURE `addUpdateIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250),
  IN in_new_name varchar(250),
  IN in_topic_in varchar(250),
  IN in_topic_out varchar(250)
)
BEGIN
    INSERT INTO `intent` (`aiid`, `name`, `topic_in`, `topic_out`)
      SELECT `aiid`, `in_name`, `in_topic_in`, `in_topic_out`
      FROM ai
      WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `topic_in`=`in_topic_in`, `topic_out`=`in_topic_out`, `name`=`in_new_name`;
  END ;;
DELIMITER ;


DROP PROCEDURE `getIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`, `last_updated`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
		AND `intent`.`aiid` = `in_aiid`;
END;;
DELIMITER ;



DROP PROCEDURE `setChatState`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE,
  IN `param_chat_target` TINYINT(1),
  IN `param_handover_reset` TIMESTAMP,
  IN `param_bad_answers_count` INT(11))
BEGIN
    INSERT INTO chatState (dev_id, chat_id, topic, history, locked_aiid, entity_values, confidence_threshold, chat_target, 
      handover_reset, bad_answers_count)
    VALUES(param_devId, param_chatId, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold, 
      param_chat_target, param_handover_reset, param_bad_answers_count)
    ON DUPLICATE KEY UPDATE topic = param_topic, history = param_history,
      locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold, 
      chat_target = param_chat_target, handover_reset = param_handover_reset, bad_answers_count = param_bad_answers_count;
  END ;;
DELIMITER ;



DROP PROCEDURE `getAi`;

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


DROP PROCEDURE `getAIs`;

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


DROP PROCEDURE `addAi`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_name` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_client_token` VARCHAR(250),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` BOOLEAN,
  IN `param_ui_ai_voice` INT(11),
  IN `param_error_threshold_handover` int(11),
  IN `param_handover_reset_timeout` int(11),
  IN `param_handover_message` varchar(2048))
    MODIFIES SQL DATA
BEGIN

  DECLARE var_exists_count INT;
  DECLARE var_named_aiid VARCHAR(50);

  SELECT count(aiid), min(aiid) INTO var_exists_count, var_named_aiid
  FROM ai WHERE `param_dev_id`=`ai`.`dev_id` AND `param_ai_name`=`ai`.`ai_name` AND `ai`.`deleted` = 0;

  IF var_exists_count=0 THEN
    INSERT INTO ai (aiid, ai_name, ai_description, dev_id, is_private,
                    client_token,
                    ui_ai_language, ui_ai_timezone, ui_ai_confidence, ui_ai_personality, ui_ai_voice,
                    default_chat_responses, handover_message, handover_reset_timeout, error_threshold_handover)
    VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,
                        param_client_token,
                        param_ui_ai_language,
                        param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice,
            '["Erm...What?"]', param_handover_message, param_handover_reset_timeout, param_error_threshold_handover);
    SET var_named_aiid = `param_aiid`;
  END IF;

  SELECT var_named_aiid AS aiid;

  END ;;
DELIMITER ;


DROP PROCEDURE `updateAi`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` TINYINT(4),
  IN `param_ui_ai_voice` VARCHAR(50),
  IN `param_default_chat_responses` TEXT,
  IN `param_error_threshold_handover` int(11),
  IN `param_handover_reset_timeout` int(11),
  IN `param_handover_message` varchar(2048))
    MODIFIES SQL DATA
BEGIN

    update ai
    set
      ai_description = param_ai_description,
      is_private = param_is_private,
      ui_ai_language = param_ui_ai_language,
      ui_ai_timezone = param_ui_ai_timezone,
      ui_ai_confidence = param_ui_ai_confidence,
      ui_ai_personality = param_ui_ai_personality,
      ui_ai_voice = param_ui_ai_voice,
      default_chat_responses = param_default_chat_responses,
      handover_message = param_handover_message,
      handover_reset_timeout = param_handover_reset_timeout,
      error_threshold_handover = param_error_threshold_handover
    where aiid = param_aiid AND dev_id = param_dev_id;
  END ;;
DELIMITER ;


DROP PROCEDURE `addUser`;

DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUser`(
  IN `param_dev_token` VARCHAR(250),
  IN `param_plan_id` INT,
  IN `param_dev_id` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    INSERT INTO `users`(`dev_token`, `plan_id`, `dev_id`)
    VALUES (param_dev_token, param_plan_id, param_dev_id);
  END ;;
DELIMITER ;


DROP PROCEDURE `addAi`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_name` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_client_token` VARCHAR(250),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` BOOLEAN,
  IN `param_ui_ai_voice` INT(11),
  IN `param_default_chat_responses` TEXT,
  IN `param_error_threshold_handover` int(11),
  IN `param_handover_reset_timeout` int(11),
  IN `param_handover_message` varchar(2048))
    MODIFIES SQL DATA
BEGIN

  DECLARE var_exists_count INT;
  DECLARE var_named_aiid VARCHAR(50);

  SELECT count(aiid), min(aiid) INTO var_exists_count, var_named_aiid
  FROM ai WHERE `param_dev_id`=`ai`.`dev_id` AND `param_ai_name`=`ai`.`ai_name` AND `ai`.`deleted` = 0;

  IF var_exists_count=0 THEN
    INSERT INTO ai (aiid, ai_name, ai_description, dev_id, is_private,
                    client_token,
                    ui_ai_language, ui_ai_timezone, ui_ai_confidence, ui_ai_personality, ui_ai_voice,
                    default_chat_responses, handover_message, handover_reset_timeout, error_threshold_handover)
    VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,
                        param_client_token,
                        param_ui_ai_language,
                        param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice,
            param_default_chat_responses, param_handover_message, param_handover_reset_timeout, param_error_threshold_handover);
    SET var_named_aiid = `param_aiid`;
  END IF;

  SELECT var_named_aiid AS aiid;

  END ;;
DELIMITER ;