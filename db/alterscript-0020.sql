USE hutoma;

DROP TABLE IF EXISTS `intent_response`;
DROP TABLE IF EXISTS `intent_user_says`;
DROP TABLE IF EXISTS `intent_variable_prompt`;
DROP TABLE IF EXISTS `intent_variable`;

DROP PROCEDURE IF EXISTS `addIntentResponse`;
DROP PROCEDURE IF EXISTS `deleteIntentResponse`;
DROP PROCEDURE IF EXISTS `getIntentResponses`;
DROP PROCEDURE IF EXISTS `addIntentUserSays`;
DROP PROCEDURE IF EXISTS `deleteIntentUserSays`;
DROP PROCEDURE IF EXISTS `getIntentUserSays`;
DROP PROCEDURE IF EXISTS `addIntentVariablePrompt`;
DROP PROCEDURE IF EXISTS `addUpdateIntentVariable`;
DROP PROCEDURE IF EXISTS `deleteIntentVariable`;
DROP PROCEDURE IF EXISTS `getIntentVariableIDs`;
DROP PROCEDURE IF EXISTS `getIntentVariables`;
DROP PROCEDURE IF EXISTS `addIntentVariablePrompt`;
DROP PROCEDURE IF EXISTS `deleteIntentVariablePrompt`;
DROP PROCEDURE IF EXISTS `getIntentVariablePrompts`;

DROP PROCEDURE IF EXISTS `deleteEntity`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_entity_id int(11))
BEGIN
    DELETE FROM `entity`
    WHERE `in_dev_id`=`dev_id` AND `in_entity_id`=`id`;
END ;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `deleteAllMemoryIntents`;
DROP PROCEDURE IF EXISTS `deleteMemoryIntent`;
DROP PROCEDURE IF EXISTS `existsAiTrainingFile`;
DROP PROCEDURE IF EXISTS `getAiIntegratedResource`;
DROP PROCEDURE IF EXISTS `getAiStatusAll`;
DROP PROCEDURE IF EXISTS `getAiStatusForUpdate`;
DROP PROCEDURE IF EXISTS `getAiVoice`;
DROP PROCEDURE IF EXISTS `getDevToken`;
DROP PROCEDURE IF EXISTS `getDomains`;
DROP PROCEDURE IF EXISTS `getIntentIDs`;
DROP PROCEDURE IF EXISTS `getIntent_toDeprecate`;
DROP PROCEDURE IF EXISTS `getMemoryIntent`;
DROP PROCEDURE IF EXISTS `getMemoryIntentsForChat`;
DROP PROCEDURE IF EXISTS `updateMemoryIntent`;