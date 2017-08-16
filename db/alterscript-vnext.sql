/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `ai` ADD COLUMN `default_chat_responses` JSON NOT NULL AFTER `ai_description`;
UPDATE `ai` SET `default_chat_responses`='["Erm... What?"]';



drop procedure `addAi`;

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
  IN `param_ui_ai_voice` INT(11))
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
                      default_chat_responses)
      VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,
                          param_client_token,
                          param_ui_ai_language,
                          param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice,
              '["Erm...What?"]');
      SET var_named_aiid = `param_aiid`;
    END IF;

    SELECT var_named_aiid AS aiid;

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
      `default_chat_responses`,
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



DROP PROCEDURE `getAIs`;
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
  IN `param_default_chat_responses` TEXT)
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
      default_chat_responses = param_default_chat_responses
    where aiid = param_aiid AND dev_id = param_dev_id;

  END ;;
DELIMITER ;

