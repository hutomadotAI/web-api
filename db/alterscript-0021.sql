ALTER DATABASE hutoma CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;

USE hutoma;

# Turn off foreign key checks
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE `hutoma`.`ai` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`ai_integration` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`ai_memory` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`ai_status` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`ai_training` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`api_rate_limit` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`botIcon` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`botPurchase` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`botStore` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`botTemplate` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`bot_ai` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`bot_ai_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`chatState` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`controller_state` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`debug` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`developerInfo` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`devplan` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`domains` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`entity` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`entity_value` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`feature_toggle` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`integrations` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`intent` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`migration_status` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`users` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;
ALTER TABLE `hutoma`.`webhooks` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci;


SET FOREIGN_KEY_CHECKS = 1;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
--
-- Dumping routines (PROCEDURE) for database 'hutoma'
--
DELIMITER ;;

# Dump of PROCEDURE addAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `addAi`(
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

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addBotTemplate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addBotTemplate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `addBotTemplate`(
  IN `param_botId` INT(11),
  IN `param_template` TEXT
)
BEGIN
    INSERT INTO botTemplate (botId, template) VALUES (param_botId, param_template);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addEntityValue
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addEntityValue` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250))
BEGIN
    INSERT INTO `entity_value` (`entity_id`,`value`)
      SELECT `id`, `in_value` FROM `entity`
      WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addIntentResponse
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addIntentResponse` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(1000))
BEGIN
    INSERT INTO `intent_response` (`intent_id`, `response`)
      SELECT `id`, `in_response` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                               (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addIntentUserSays
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addIntentUserSays` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(1000))
BEGIN
    INSERT INTO `intent_user_says` (`intent_id`, `says`)
      SELECT `id`, `in_says` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                           (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addIntentVariablePrompt
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addIntentVariablePrompt` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addIntentVariablePrompt`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT,
  IN in_prompt VARCHAR(250)
)
BEGIN

    INSERT INTO `intent_variable_prompt` (`intent_variable_id`, `prompt`)
      SELECT `intent_variable`.`id`, `in_prompt`
      FROM `intent_variable`, `intent`
      WHERE `in_intent_variable_id` = `intent_variable`.`id`
            AND `intent_variable`.`intent_id` = `intent`.`id`
            AND `in_aiid` = `intent`.`aiid`
            AND `intent`.`aiid` IN
                (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addUpdateEntity
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addUpdateEntity` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addUpdateEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_new_name VARCHAR(250))
BEGIN
    INSERT INTO `entity` (`dev_id`, `name`) VALUES (`in_dev_id`, `in_name`)
    ON DUPLICATE KEY UPDATE `name`=`in_new_name`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addUpdateIntent
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addUpdateIntent` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addUpdateIntent`(
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addUpdateIntentVariable
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addUpdateIntentVariable` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `addUpdateIntentVariable`(
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

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addUser
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addUser` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `addUser`(
  IN `param_dev_token` VARCHAR(250),
  IN `param_plan_id` INT,
  IN `param_dev_id` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    INSERT INTO `users`(`dev_token`, `plan_id`, `dev_id`)
    VALUES (param_dev_token, param_plan_id, param_dev_id);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE addWebhook
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `addWebhook` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `addWebhook`(
    IN `aiid` VARCHAR(50), 
    IN `intent_name` VARCHAR(250), 
    IN `endpoint` VARCHAR(2048), 
    IN `enabled` INT(1))
    MODIFIES SQL DATA
BEGIN
INSERT INTO `webhooks`(`aiid`, `intent_name`, `endpoint`, `enabled`)
VALUES (aiid, intent_name, endpoint, enabled);
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE checkIntegrationUser
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `checkIntegrationUser` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `checkIntegrationUser`(
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_devid` VARCHAR(50))
BEGIN

SELECT COUNT(`dev_id`) AS 'use_count'
FROM `ai_integration` 
INNER JOIN `ai` ON `ai_integration`.`aiid`=`ai`.`aiid`
WHERE `integration`=`in_integration`
AND `integrated_userid` = `in_integrated_userid`
AND `dev_id`!=`in_devid`;
    
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiDeleter`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1
        WHERE `dev_id`=`in_dev_id` AND `aiid`=`in_aiid`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteAiStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteAiStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiDeleter`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    DELETE FROM `ai_status` 
        WHERE `ai_status`.`server_type`=`in_server_type`
        AND `ai_status`.`aiid` = `in_aiid`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteAllAIs
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteAllAIs` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiDeleter`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1 WHERE `dev_id`=param_devid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteEntity
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteEntity` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_entity_id int(11))
BEGIN
    DELETE FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_entity_id`=`id`
      AND NOT EXISTS (
     SELECT NULL FROM `intent_variable` iv INNER JOIN `intent` ON `intent`.`id`=iv.`intent_id` INNER JOIN `ai` ON `ai`.`aiid`=`intent`.`aiid` 
     WHERE iv.`entity_id`=`in_entity_id` AND `ai`.`deleted`=0);
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteEntityValue
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteEntityValue` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250))
BEGIN
    DELETE FROM `entity_value` WHERE `in_value`=`value` AND `entity_id`=
                                                            (SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntegration
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntegration` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

DELETE `ai_integration` 
FROM `ai_integration` INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntent
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntent` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250)
)
BEGIN
    DELETE FROM `intent`
    WHERE `in_aiid`=`aiid`
          AND `in_name`=`name`
          AND `aiid` IN
              (SELECT `aiid` FROM ai
              WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntentResponse
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentResponse` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(1000))
BEGIN
    DELETE FROM `intent_response`
    WHERE `in_response`=`response` AND `intent_id`=
                                       (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                                  (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntentUserSays
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentUserSays` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(1000))
BEGIN
    DELETE FROM `intent_user_says`
    WHERE `in_says`=`says` AND `intent_id`=
                               (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                          (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntentVariable
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentVariable` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntentVariable`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT
)
BEGIN

    DELETE FROM `intent_variable`
    WHERE `in_intent_variable_id`=`intent_variable`.`id`
          AND `intent_id` IN
              (SELECT `id` FROM `intent` WHERE `in_aiid`=`aiid` AND `in_aiid` IN
                                                                    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteIntentVariablePrompt
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentVariablePrompt` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteIntentVariablePrompt`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT,
  IN in_prompt VARCHAR(250)
)
BEGIN

    DELETE FROM `intent_variable_prompt` WHERE
      `intent_variable_prompt`.`intent_variable_id`=`in_intent_variable_id` AND
      `intent_variable_prompt`.`prompt`=`in_prompt` AND
      `in_intent_variable_id` IN
      (SELECT `intent_variable`.`id`
       FROM `intent_variable`, `intent`
       WHERE `intent_variable`.`intent_id` = `intent`.`id`
             AND `in_aiid` = `intent`.`aiid`
             AND `in_aiid` IN
                 (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteUser
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteUser` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userDeleter`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteUser`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
    delete from users where dev_id=param_devid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE deleteWebhook
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `deleteWebhook` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `deleteWebhook`(
    IN `param_aiid` VARCHAR(50), IN `param_intent_name` VARCHAR(250))
    READS SQL DATA
BEGIN
  DELETE FROM `webhooks`
  WHERE `webhooks`.`intent_name`=`param_intent_name`
  AND `webhooks`.`aiid`=`param_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE existsAiTrainingFile
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `existsAiTrainingFile` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `existsAiTrainingFile`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN

    SELECT count(aiid) as `ai_trainingfile`
    FROM ai_training WHERE `param_aiid`=`ai_training`.`aiid`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE fill_missing_labels
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `fill_missing_labels` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`root`@`%`*/ /*!50003 PROCEDURE `fill_missing_labels`()
BEGIN
DECLARE v_finished INTEGER DEFAULT 0;
DECLARE v_iv_id INTEGER;
DECLARE v_e_name VARCHAR(50);
DECLARE cursor1 CURSOR FOR 
    SELECT iv.id, e.name from intent_variable iv inner join entity e on e.id=iv.entity_id
    where iv.label = '' or iv.label is null;
DECLARE CONTINUE HANDLER FOR 
    NOT FOUND SET v_finished = 1;
OPEN cursor1;
myLoop: LOOP
    FETCH cursor1 INTO v_iv_id, v_e_name;
    IF v_finished = 1 THEN 
        LEAVE myLoop;
    END IF;
    UPDATE intent_variable SET label = CONCAT(v_e_name, '_label') WHERE id = v_iv_id;
END LOOP myLoop;
CLOSE cursor1;  
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAi`(
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiBotConfig
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiBotConfig` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiBotConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_bot_id` INT(11))
BEGIN
    SELECT * FROM `bot_ai_config` WHERE `dev_id` = `in_dev_id` AND `aiid` = `in_aiid` AND `botId` = `in_bot_id`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiIntegration
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiIntegration` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

SELECT `integrated_resource`, `integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`;
    
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiIntegrationForUpdate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiIntegrationForUpdate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiIntegrationForUpdate`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

SELECT `integrated_resource`, `integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`
FOR UPDATE;
    
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAIQueueStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAIQueueStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAIQueueStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`server_type`,
        `ai_status`.`aiid`,
        `ai`.`dev_id`,
        `ai_status`.`training_status`,
        `ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_time`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`,
        `ai`.`deleted`
        FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid` = `in_aiid`
    AND `ai_status`.`server_type` = `in_server_type`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAIs
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAIs` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAIs`(
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAIsForEntity
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAIsForEntity` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAIsForEntity`(IN `param_devid` VARCHAR(50), IN `param_entity_name` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT i.aiid FROM intent i
      INNER JOIN intent_variable iv ON i.id=iv.intent_id
      INNER JOIN entity e ON iv.entity_id = e.id
    WHERE e.name = param_entity_name
          AND e.dev_id = param_devid;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiSimple
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiSimple` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiSimple`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
      `id`,
      `dev_id`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiSkillConfig
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiSkillConfig` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiSkillConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_bot_id` INT(11))
BEGIN
    SELECT * FROM `bot_ai` WHERE `bot_ai`.`dev_id` = `in_dev_id` AND `bot_ai`.`aiid` = `in_aiid` AND `bot_ai`.`botId` = `in_bot_id`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAisLinkedToAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAisLinkedToAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAisLinkedToAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50))
BEGIN
    SELECT bai.aiid as 'ai', bs.aiid as 'linked_ai', bs.dev_id as 'linked_ai_devId', ai.ui_ai_confidence as 'minP'
    FROM bot_ai bai INNER JOIN botStore bs ON bs.id = bai.botId INNER JOIN ai ai ON ai.aiid = bs.aiid
    WHERE bai.aiid=param_aiid AND bai.dev_id=param_devId AND botId != 0;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAIsServerStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAIsServerStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAIsServerStatus`(
  IN `in_server_type` VARCHAR(10))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`aiid`,
        `ai`.`dev_id`,
        `ai_status`.`training_status`,
        `ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_time`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`deleted` = 0
    AND `ai_status`.`server_type` = `in_server_type`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAIsStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAIsStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAIsStatus`(
  IN `in_dev_id` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`server_type`,
        `ai_status`.`aiid`,
        `ai_status`.`training_status`,
        `ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`dev_id` = `in_dev_id`
    AND `ai`.`deleted` = 0;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiStatus`(
  IN `param_aiid` VARCHAR(50),
  IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`server_type`,
        `ai_status`.`aiid`,
        `ai_status`.`training_status`, 
        `ai_status`.`training_progress`, 
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`        
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid`=`param_aiid`
    AND `ai`.`dev_id`=`param_devid`
    AND `ai`.`deleted` = 0;    
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiStatusAll
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiStatusAll` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiStatusAll`()
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`server_type`,
        `ai_status`.`aiid`,
        `ai_status`.`training_status`,
        `ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`        
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`deleted` = 0;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiStatusForUpdate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiStatusForUpdate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiStatusForUpdate`(
  IN `param_aiid` VARCHAR(50),
  IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`server_type`,
        `ai_status`.`aiid`,
        `ai_status`.`training_status`, 
        `ai_status`.`training_progress`, 
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`        
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid`=param_aiid 
    AND `ai`.`dev_id`=param_devid
    AND `ai`.`deleted` = 0
    FOR UPDATE;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiTrainingFile
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiTrainingFile` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiTrainingFile`(IN `param_aiid` varchar(50))
    READS SQL DATA
BEGIN

    SELECT ai_trainingfile FROM ai_training WHERE aiid=param_aiid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAiVoice
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAiVoice` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAiVoice`(IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT `ai_voice`
    FROM `ai`
    WHERE `aiid`=`param_aiid`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getAllUsers
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getAllUsers` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getAllUsers`()
BEGIN
    SELECT *
    FROM `users`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotConfigForWebhookCall
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotConfigForWebhookCall` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotConfigForWebhookCall`(
    IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50),
    IN `param_aiidLinkedBot` VARCHAR(50))
    NO SQL
BEGIN
    IF `param_aiid` = `param_aiidLinkedBot` THEN
        SELECT bac.config AS `config` FROM bot_ai_config bac 
        WHERE bac.aiid = param_aiid AND bac.dev_id = param_devId AND bac.botId = 0;
    ELSE
        SELECT bac.config AS `config` FROM bot_ai_config bac 
        INNER JOIN botStore bs ON bac.botId = bs.id 
        WHERE bac.aiid = param_aiid AND bac.dev_id = param_devId AND bs.aiid = param_aiidLinkedBot;
    END IF;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotDetails
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotDetails` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotDetails`(IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE id = param_botId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotIcon
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotIcon` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotIcon`(IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT botIcon FROM botStore WHERE id = param_botId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotsLinkedToAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotsLinkedToAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotsLinkedToAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai ON bai.botId = bs.id WHERE bai.aiid = param_aiid AND bai.dev_id = param_devId AND bai.botId != 0;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotstoreItem
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreItem` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotstoreItem`(
    IN `param_botId` INT)
BEGIN

SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website', ai.api_keys_desc as 'api_keys_desc'
FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id INNER JOIN ai ON ai.aiid = bs.aiid WHERE bs.publishing_state=2 AND bs.id = param_botId;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotstoreList
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreList` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotstoreList`(
    IN `param_filters` VARCHAR(255),
    IN `param_order` VARCHAR(50),
    IN `param_pageStart` INT,
    IN `param_pageSize` INT)
BEGIN

SET @select_query = "SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website' ";
SET @from_query = "FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id WHERE publishing_state=2 AND publishing_type=1 ";
IF LENGTH(param_filters) = 0 THEN
    SET @where_other = "";
ELSE
    SET @where_other = concat(" AND ", param_filters);
END IF;
IF LENGTH(param_order) = 0 THEN
    SET @orderBy = "";
ELSE
    SET @orderBy = concat(" ORDER BY ", param_order);
END IF;
SET @limitTo = concat(concat(concat(" LIMIT ", param_pageStart), ", "), param_pageSize);
SET @query = concat(concat(concat(concat(@select_query, @from_query), @where_other), @orderBy), @limitTo);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;


SET @select_query = "SELECT COUNT(*) as 'total'";
SET @query = concat(concat(@select_query, @from_query), @where_other);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotstoreListPerCategory
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreListPerCategory` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotstoreListPerCategory`(
    IN `param_max` INT(11))
BEGIN
    SET @select_query =
    "SELECT * FROM(
        SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website',
            (@num:=if(@group = bs.category, @num +1, if(@group := bs.category, 1, 1))) row_number
        FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id
        CROSS JOIN (select @num:=0, @group:=null) c
        WHERE publishing_state=2 AND publishing_type=1
        ORDER BY bs.category
    ) as x
    WHERE x.row_number <= ";

    SET @limitTo = param_max;

    SET @query = concat(@select_query, @limitTo);

    PREPARE stmt3 FROM @query;
    EXECUTE stmt3;
    DEALLOCATE PREPARE stmt3;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getBotTemplate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getBotTemplate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getBotTemplate`(
  IN `param_botId` INT(11)
)
BEGIN
    SELECT `template` FROM botTemplate WHERE `botId` = param_botId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getChatState
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getChatState` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `getChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50))
BEGIN
    SELECT * FROM chatState WHERE dev_id = param_devId AND chat_id = param_chatId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getClientToken
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getClientToken` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`hutoma_caller`@`127.0.0.1`*/ /*!50003 PROCEDURE `getClientToken`(IN `id` INT)
    NO SQL
BEGIN
    SELECT `client_token` FROM `users`
    WHERE `id`=id;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDebug
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDebug` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`root`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDebug`(IN `l1` INT UNSIGNED)
    READS SQL DATA
    COMMENT 'this is a test procedure to help confirm functionality'
SELECT *
  FROM `debug`
  LIMIT 0 , l1 */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDeveloperInfo
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDeveloperInfo` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDeveloperInfo`(IN `param_devid` VARCHAR(50))
BEGIN
    SELECT * FROM developerInfo WHERE dev_id = param_devid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDevPlan
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDevPlan` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDevPlan`(IN `in_dev_id` VARCHAR(50))
BEGIN
    SELECT d.*
    FROM devplan d INNER JOIN users u ON u.plan_id = d.plan_id
    WHERE u.dev_id=in_dev_id;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDevToken
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDevToken` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDevToken`(IN `uid` INT)
    NO SQL
BEGIN
    SELECT `dev_token` FROM `users` WHERE `id` = uid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDevTokenFromDevID
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDevTokenFromDevID` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDevTokenFromDevID`(IN `devid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT `dev_token` FROM `users` WHERE `dev_id` = devid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getDomains
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getDomains` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`domainsReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getDomains`()
    READS SQL DATA
BEGIN
    SELECT * FROM `domains`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getEntities
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getEntities` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getEntities`(
  IN in_dev_id VARCHAR(50))
BEGIN
    SELECT `name`, `isSystem` FROM `entity` WHERE 
    `entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1;    
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getEntityDetails
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getEntityDetails` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getEntityDetails`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT * FROM `entity`
    WHERE `entity`.`name`=`in_name`
          AND (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getEntityIdForDev
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getEntityIdForDev` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getEntityIdForDev`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getEntityValues
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getEntityValues` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`entityUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getEntityValues`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `entity_value`.`value` FROM `entity`,`entity_value`
    WHERE `entity`.`dev_id`=`in_dev_id`
          AND `entity`.`name`=`in_name`
          AND `entity`.`id`=`entity_value`.`entity_id`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getFeatures
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getFeatures` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getFeatures`()
BEGIN
    SELECT `devid`, `aiid`, `feature`, `state`
    FROM `feature_toggle`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntegratedResource
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntegratedResource` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntegratedResource`(
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250))
BEGIN

SELECT `ai_integration`.`aiid` AS `aiid`, `ai`.`dev_id` AS `devid`,
        `integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `integration`=`in_integration`
AND `integrated_resource`=`in_integrated_resource`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntegrations
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntegrations` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`integrReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntegrations`()
    READS SQL DATA
BEGIN
    SELECT * FROM `integrations`;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntent
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntent` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `aiid`, `name`, `intent_json`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`aiid` = `in_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentIDs
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentIDs` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentIDs`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50)
)
BEGIN

    SELECT `intent`.`id` from `intent`, `ai`
    WHERE `ai`.`dev_id` = `in_dev_id`
          AND `ai`.`aiid` = `in_aiid`
          AND `intent`.`aiid` = `in_aiid`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentResponses
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentResponses` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentResponses`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `response`
    FROM `intent_response` WHERE `intent_id` IN
         (SELECT `id` FROM `intent` 
          WHERE `in_name`=`name` AND `in_aiid`=`aiid`);
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntents
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntents` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntents`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT `id`, `name`
    FROM `intent` WHERE `intent`.`id` IN
                        (SELECT `intent`.`id` from `intent`, `ai`
                        WHERE `ai`.`dev_id` = `in_dev_id`
                              AND `ai`.`aiid` = `in_aiid`
                              AND `intent`.`aiid` = `in_aiid`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentsDetails
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentsDetails` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentsDetails`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT `id`, `name`
    FROM `intent` WHERE `intent`.`id` IN
                        (SELECT `intent`.`id` from `intent`, `ai`
                        WHERE `ai`.`dev_id` = `in_dev_id`
                              AND `ai`.`aiid` = `in_aiid`
                              AND `intent`.`aiid` = `in_aiid`);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentUserSays
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentUserSays` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentUserSays`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `says`
    FROM `intent_user_says` WHERE `intent_id` IN
         (SELECT `id` FROM `intent` 
          WHERE `in_name`=`name` AND `in_aiid`=`aiid`);
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentVariableIDs
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariableIDs` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentVariableIDs`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_id INT
)
BEGIN

    SELECT `intent_variable`.`id` FROM `intent_variable`, `intent`, `ai`
    WHERE `ai`.`dev_id` = `in_dev_id`
          AND `ai`.`aiid` = `in_aiid`
          AND `intent`.`aiid` = `in_aiid`
          AND `intent_variable`.`intent_id` = `in_intent_id`
          AND `intent_variable`.`intent_id` = `intent`.`id`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentVariablePrompts
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariablePrompts` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentVariablePrompts`(
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT
)
BEGIN

    SELECT `prompt` FROM `intent_variable_prompt` WHERE
      `intent_variable_prompt`.`intent_variable_id`=`in_intent_variable_id` AND
      `in_intent_variable_id` IN
      (SELECT `intent_variable`.`id`
       FROM `intent_variable`, `intent`
       WHERE `intent_variable`.`intent_id` = `intent`.`id`
             AND `in_aiid` = `intent`.`aiid`);

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntentVariables
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariables` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntentVariables`(
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

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIntent_toDeprecate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIntent_toDeprecate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`intentUser`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIntent_toDeprecate`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`, `last_updated`, `context_in`, `context_out`, `conditions_in`,
    `conditions_default_response`, `reset_context_on_exit`, `transitions`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`aiid` = `in_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getInterruptedTrainingList
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getInterruptedTrainingList` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getInterruptedTrainingList`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(50),
  IN `in_cutoff_seconds` INT)
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL `in_cutoff_seconds` SECOND);

SELECT `ai_status`.*
FROM `ai_status`
WHERE `server_type` = `in_server_type`
AND `ai_status`.`queue_time` IS NULL
AND `ai_status`.`training_status` = `in_training_status`
AND `update_time`<=v_cutoff
ORDER BY `update_time` ASC
FOR UPDATE;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getIsBotLinkedToAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getIsBotLinkedToAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getIsBotLinkedToAi`(
    IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50),
    IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai ON bai.botId = bs.id WHERE bai.aiid = param_aiid AND bai.dev_id = param_devId AND bai.botId = param_botId;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getPublishedBotForAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getPublishedBotForAi` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getPublishedBotForAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs
    WHERE bs.dev_id = param_devId AND bs.aiid = param_aiid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getPublishedBotIdForAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getPublishedBotIdForAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getPublishedBotIdForAi`(IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.id FROM botStore bs
    WHERE bs.aiid = param_aiid;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getPublishedBots
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getPublishedBots` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getPublishedBots`(
IN `param_publishing_type` TINYINT(1))
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE publishing_state = 2 AND publishing_type = param_publishing_type;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getPurchasedBots
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getPurchasedBots` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getPurchasedBots`(IN `param_devId` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botPurchase bp INNER JOIN botStore bs ON bs.id = bp.botId WHERE bp.dev_id = param_devId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getUserFromDevId
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getUserFromDevId` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getUserFromDevId`(IN `param_devId` VARCHAR(50))
BEGIN
    SELECT *
    FROM `users`
    WHERE `dev_id`=param_devId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getWebhook
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getWebhook` */;;
/*!50003 SET SESSION SQL_MODE="STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getWebhook`(
    IN `param_aiid` VARCHAR(50), IN `param_intent_name` VARCHAR(250))
    READS SQL DATA
BEGIN
  SELECT
    `aiid`,
    `intent_name`,
    `endpoint`,
    `enabled`
  FROM `webhooks`
  WHERE `webhooks`.`intent_name`=`param_intent_name` AND `webhooks`.`aiid`=`param_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE getWebhookSecretForBot
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `getWebhookSecretForBot` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `getWebhookSecretForBot`(IN `param_aiid` VARCHAR(50))
BEGIN
    SELECT `hmac_secret` FROM `ai` WHERE `aiid` = `param_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE hasBotBeenPurchased
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `hasBotBeenPurchased` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `hasBotBeenPurchased`(IN `param_botId` INT(11))
BEGIN
    SELECT EXISTS (SELECT botId FROM botPurchase WHERE botId = param_botId);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE linkBotToAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `linkBotToAi` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `linkBotToAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    INSERT INTO bot_ai (botId, dev_id, aiid) VALUES(param_botId, param_devId, param_aiid);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE publishBot
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `publishBot` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `publishBot`(
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE purchaseBot
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `purchaseBot` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `purchaseBot`(IN `param_devId` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    DECLARE thePrice DECIMAL;
    SELECT price INTO thePrice FROM botStore WHERE id = param_botId;
    INSERT INTO botPurchase (botId, dev_id, price) VALUES (param_botId, param_devId, thePrice);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE queueCountSlots
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `queueCountSlots` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `queueCountSlots`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(50),
  IN `in_cutoff_seconds` INT)
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL `in_cutoff_seconds` SECOND);

SELECT `ai_status`.`server_endpoint`,
 sum(case when `ai_status`.`update_time` > v_cutoff then 1 else 0 end) training,
 sum(case when `ai_status`.`update_time` <= v_cutoff then 1 else 0 end) lapsed
FROM `ai_status`
WHERE `server_type` = `in_server_type`
AND `ai_status`.`queue_time` IS NULL
AND `ai_status`.`training_status` = `in_training_status`
GROUP BY `ai_status`.`server_endpoint`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE queueRecover
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `queueRecover` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `queueRecover`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),  
  IN `in_queue_action` VARCHAR(50),
  IN `in_training_status` VARCHAR(45)
  )
BEGIN
 
UPDATE `ai_status` SET  
    `training_status` = `in_training_status`,
    `queue_action`=`in_queue_action`,
    `update_time`=now(),
    `queue_time`=now()
WHERE `server_type` = `in_server_type` 
    AND `aiid` = `in_aiid`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE queueTakeNext
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `queueTakeNext` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `queueTakeNext`(
  IN `in_server_type` VARCHAR(10))
BEGIN

DECLARE v_aiid VARCHAR(50);
SELECT `ai_status`.`aiid` INTO v_aiid 
    FROM `ai_status` 
    WHERE `server_type` = `in_server_type` 
    AND `queue_time`<now()
    ORDER BY `queue_time` ASC
    LIMIT 1 FOR UPDATE;

IF NOT v_aiid IS NULL THEN
    UPDATE `ai_status` SET `queue_time`=NULL 
        WHERE `aiid` = v_aiid
        AND `in_server_type`=`server_type`;
END IF;        
SELECT * FROM `ai_status` 
    WHERE v_aiid IS NOT NULL
    AND `aiid`=v_aiid
    AND `in_server_type`=`server_type`;


  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE queueUpdate
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `queueUpdate` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `queueUpdate`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_set_queued` TINYINT,
  IN `in_queue_offset` INT,
  IN `in_queue_action` VARCHAR(50)
  )
BEGIN
 
DECLARE v_queue_time DATETIME;
IF (`in_set_queued` = 0) THEN
    SET v_queue_time = NULL;
ELSE
    SET v_queue_time = now() + INTERVAL `in_queue_offset` SECOND;
END IF;

UPDATE `ai_status` SET  
    `queue_time`=v_queue_time,
    `queue_action`=`in_queue_action`,
    `update_time`=now()
WHERE `server_type` = `in_server_type` 
    AND `aiid` = `in_aiid`;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE rateLimitCheck
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `rateLimitCheck` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`rateLimiter`@`127.0.0.1`*/ /*!50003 PROCEDURE `rateLimitCheck`(
  IN `in_dev_id` VARCHAR(50) CHARSET utf8,
  IN `in_rate_key` VARCHAR(50) CHARSET utf8,
  IN `token_ceiling` FLOAT,
  IN `token_increment_delay_seconds` FLOAT)
    MODIFIES SQL DATA
BEGIN

    DECLARE time_now BIGINT;
    DECLARE var_uuid VARCHAR(50);
    DECLARE user_valid tinyint;

    SET var_uuid = uuid();
    SET time_now = CONV(CONCAT(SUBSTR(var_uuid, 16, 3),SUBSTR(var_uuid, 10, 4),SUBSTR(var_uuid, 1, 8)), 16, 10) / 10000 - (141427 * 24 * 60 * 60);

    SELECT count(*) INTO user_valid
    FROM users
    WHERE users.dev_id = in_dev_id
          AND users.valid > 0;

    IF NOT user_valid THEN
      SELECT 1 AS rate_limit, 0.0 AS tokens, 0 AS valid;
    ELSE
      INSERT INTO api_rate_limit (dev_id, rate_key, tokens, token_update_time)
      VALUES (in_dev_id, in_rate_key, token_ceiling, time_now)
      ON DUPLICATE KEY UPDATE
        tokens = LEAST(tokens + (time_now - token_update_time)/(1000.0 * token_increment_delay_seconds), token_ceiling),
        token_update_time = time_now;

      UPDATE api_rate_limit SET
        tokens = tokens-1.0,
        expires = now() + INTERVAL (token_ceiling * token_increment_delay_seconds) SECOND
      WHERE
        (dev_id = in_dev_id AND rate_key = in_rate_key)
        AND tokens >= 1.0;

      SELECT IF(ROW_COUNT()>0, 0, 1) AS rate_limit, tokens, 1 AS valid
      FROM api_rate_limit
      WHERE dev_id = in_dev_id AND rate_key = in_rate_key;
    END IF;

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE resetChatStatesForAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `resetChatStatesForAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `resetChatStatesForAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50)
)
BEGIN
  UPDATE `chatState`
  SET 
    `context` = NULL,
    `current_intents` = NULL,
    `bad_answers_count` = 0
  WHERE `dev_id`=param_devId AND `base_aiid`=param_aiid;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE saveBotIcon
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `saveBotIcon` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`botStoreWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `saveBotIcon`(IN `param_devId` VARCHAR(50), IN `param_botId` INT(11), IN `param_filename` VARCHAR(255))
    NO SQL
BEGIN
    UPDATE botStore
    SET  botIcon = param_filename
    WHERE dev_id = param_devId AND id = param_botId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE setAiBotConfig
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `setAiBotConfig` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `setAiBotConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_botId` INT(11),
  IN `in_config` JSON)
    MODIFIES SQL DATA
BEGIN
  INSERT INTO `bot_ai_config` SET
    `dev_id` = `in_dev_id`,
    `aiid` = `in_aiid`,
    `botId` = `in_botId`,
    `config`= `in_config`
  ON DUPLICATE KEY UPDATE `config` = `in_config`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE setApiKeyDescriptions
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `setApiKeyDescriptions` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `setApiKeyDescriptions`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_api_keys_desc` JSON)
    MODIFIES SQL DATA
BEGIN
    UPDATE ai
    SET `api_keys_desc` = `in_api_keys_desc` 
    WHERE aiid = in_aiid AND dev_id = in_dev_id;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE setChatState
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `setChatState` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_base_aiid` VARCHAR(50),
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE,
  IN `param_chat_target` TINYINT(1),
  IN `param_handover_reset` TIMESTAMP,
  IN `param_bad_answers_count` INT(11),
  IN `param_context` MEDIUMTEXT,
  IN `param_current_intents` MEDIUMTEXT)
BEGIN
  INSERT INTO chatState (dev_id, chat_id, base_aiid, topic, history, locked_aiid, entity_values, confidence_threshold, chat_target, 
    handover_reset, bad_answers_count, context, current_intents)
  VALUES(param_devId, param_chatId, param_base_aiid, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold, 
    param_chat_target, param_handover_reset, param_bad_answers_count, param_context, param_current_intents)
  ON DUPLICATE KEY UPDATE base_aiid = param_base_aiid, topic = param_topic, history = param_history,
    locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold, 
    chat_target = param_chat_target, handover_reset = param_handover_reset, bad_answers_count = param_bad_answers_count,
    context = param_context, current_intents = param_current_intents;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE setDeveloperInfo
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `setDeveloperInfo` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `setDeveloperInfo`(
  IN `param_devid` VARCHAR(50),
  IN `param_name` varchar(100),
  IN `param_company` varchar(100),
  IN `param_email` varchar(100),
  IN `param_address` varchar(200),
  IN `param_postCode` varchar(100),
  IN `param_city` varchar(100),
  IN `param_country` varchar(100),
  IN `param_website` varchar(1024))
BEGIN
    INSERT INTO developerInfo
    (`dev_id`, `name`,`company`,`email`,`address`,`post_code`,`city`,`country`,`website`)
    VALUES (param_devid,param_name,param_company,param_email,param_address,param_postCode,param_city,param_country,param_website);
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE setWebhookSecretForBot
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `setWebhookSecretForBot` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `setWebhookSecretForBot`(
  IN `in_aiid` VARCHAR(50),
  IN `in_hmac_secret` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `hmac_secret` = `in_hmac_secret`
        WHERE `aiid`=`in_aiid`;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE unlinkBotFromAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `unlinkBotFromAi` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `unlinkBotFromAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    DELETE FROM bot_ai
    WHERE botId = param_botId AND aiid = param_aiid AND dev_id = param_devId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateAi
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateAi` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateAi`(
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateAiIntegration
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateAiIntegration` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_data` JSON,
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
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateAiStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateAiStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_training_status` VARCHAR(45),
  IN `in_server_endpoint` VARCHAR(256),
  IN `in_training_progress` FLOAT,
  IN `in_training_error` FLOAT)
BEGIN

INSERT INTO `ai_status` 
( `server_type`,
  `aiid`,
  `training_status`,
  `training_progress`,
  `training_error`,
  `server_endpoint`)
VALUES 
( `in_server_type`,
  `in_aiid`,
  `in_training_status`,
  `in_training_progress`,
  `in_training_error`,
  `in_server_endpoint`)
ON DUPLICATE KEY UPDATE 
    `training_status`=`in_training_status`, 
    `training_progress`=`in_training_progress`, 
    `training_error`=`in_training_error`,
    `server_endpoint`=`in_server_endpoint`,
    `update_time`=now();
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateAiTrainingFile
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateAiTrainingFile` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateAiTrainingFile`(IN `param_aiid` varchar(50), IN `param_ai_trainingfile` LONGTEXT)
    MODIFIES SQL DATA
BEGIN

    INSERT INTO `ai_training` (ai_training.aiid, ai_training.ai_trainingfile, ai_training.updated)
    VALUES (`param_aiid`, `param_ai_trainingfile`, now())
    ON DUPLICATE KEY UPDATE
      ai_training.ai_trainingfile = `param_ai_trainingfile`,
      ai_training.updated = now();

  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateBotPublishingState
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateBotPublishingState` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateBotPublishingState`(IN `param_botId` INT(11), IN `param_publishingState` TINYINT(1))
BEGIN
    UPDATE botStore SET publishing_state = param_publishingState WHERE id = param_botId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateControllerState
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateControllerState` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateControllerState`(
  IN `in_server_type` VARCHAR(10),
  IN `in_verified_server_count` INT,
  IN `in_training_capacity` INT,
  IN `in_training_slots_available` INT,
  IN `in_chat_capacity` INT)
BEGIN
INSERT INTO `controller_state` 
  (`server_type`,
  `verified_server_count`,
  `training_capacity`,
  `training_slots_available`,
  `chat_capacity`,
  `update_time`)
VALUES 
  (`in_server_type`,
  `in_verified_server_count`,
  `in_training_capacity`,
  `in_training_slots_available`,
  `in_chat_capacity`,
  now())
ON DUPLICATE KEY UPDATE 
  `verified_server_count`=`in_verified_server_count`, 
  `training_capacity`=`in_training_capacity`,
  `training_slots_available`=`in_training_slots_available`,
  `chat_capacity`=`in_chat_capacity`,
  `update_time`=now();
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateDefaultChatResponses
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateDefaultChatResponses` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateDefaultChatResponses`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_default_responses TEXT)
BEGIN

  UPDATE `ai`
  SET `ai`.`default_chat_responses` = in_default_responses
  WHERE `ai`.`dev_id` = in_dev_id AND `ai`.`aiid` = in_aiid;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateDevToken
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateDevToken` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`userTableWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateDevToken`(IN `param_devId` VARCHAR(50), IN `param_devToken` VARCHAR(250))
BEGIN
    UPDATE `users` SET `dev_token` = param_devToken WHERE `dev_id` = param_devId;
  END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateIntegrationStatus
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateIntegrationStatus` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiReader`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateIntegrationStatus`(
  IN `in_aiid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_status` VARCHAR(1024),
  IN `in_set_chat_now` TINYINT)
BEGIN

UPDATE `ai_integration` SET 
 `status` = `in_status`,
 `chat_time` = CASE WHEN `in_set_chat_now`>0 THEN now() ELSE `chat_time` END,
 `update_time` = now()
 WHERE `aiid`=`in_aiid` 
 AND `integration`=`in_integration`;
 
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updatePassthroughUrl
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updatePassthroughUrl` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updatePassthroughUrl`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_passthrough_url VARCHAR(2048))
BEGIN

  UPDATE `ai`
  SET `ai`.`passthrough_url` = in_passthrough_url
  WHERE `ai`.`dev_id` = in_dev_id AND `ai`.`aiid` = in_aiid;

END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
# Dump of PROCEDURE updateWebhook
# ------------------------------------------------------------

/*!50003 DROP PROCEDURE IF EXISTS `updateWebhook` */;;
/*!50003 SET SESSION SQL_MODE="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`aiWriter`@`127.0.0.1`*/ /*!50003 PROCEDURE `updateWebhook`(
    IN `param_aiid` VARCHAR(50), 
    IN `param_intent_name` VARCHAR(250), 
    IN `param_endpoint` VARCHAR(2048), 
    IN `param_enabled` INT(1))
    MODIFIES SQL DATA
BEGIN
UPDATE webhooks
SET
    endpoint = param_endpoint,
    enabled = param_enabled
WHERE aiid = param_aiid AND intent_name = param_intent_name;
END */;;

/*!50003 SET SESSION SQL_MODE=@OLD_SQL_MODE */;;
DELIMITER ;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
