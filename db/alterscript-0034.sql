USE hutoma;

LOCK TABLES `chatState` WRITE;

ALTER TABLE `chatState` ADD COLUMN `chat_id_hash` VARCHAR(100) NULL AFTER `chat_id`;
ALTER TABLE `chatState` ADD INDEX `idx_chat_id_hash` (`chat_id_hash`);
ALTER TABLE `chatState` ADD COLUMN `webhook_sessions` LONGTEXT;
ALTER TABLE `chatState` ADD COLUMN `integration_data` LONGTEXT;

UNLOCK TABLES;

DROP PROCEDURE IF EXISTS `setChatState`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_chatId_hash` VARCHAR(100),
  IN `param_base_aiid` VARCHAR(50),
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE,
  IN `param_chat_target` TINYINT(1),
  IN `param_handover_reset` TIMESTAMP,
  IN `param_bad_answers_count` INT(11),
  IN `param_context` LONGTEXT,
  IN `param_current_intents` LONGTEXT,
  IN `param_webhook_sessions` LONGTEXT,
  IN `param_integration_data` LONGTEXT
)
BEGIN
  INSERT INTO chatState (dev_id, chat_id, chat_id_hash, base_aiid, topic, history, locked_aiid, entity_values, confidence_threshold, chat_target, 
    handover_reset, bad_answers_count, context, current_intents, webhook_sessions, integration_data)
  VALUES(param_devId, param_chatId, param_chatId_hash, param_base_aiid, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold, 
    param_chat_target, param_handover_reset, param_bad_answers_count, param_context, param_current_intents, param_webhook_sessions, param_integration_data)
  ON DUPLICATE KEY UPDATE chat_id_hash = param_chatId_hash, base_aiid = param_base_aiid, topic = param_topic, history = param_history,
    locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold, 
    chat_target = param_chat_target, handover_reset = param_handover_reset, bad_answers_count = param_bad_answers_count,
    context = param_context, current_intents = param_current_intents, webhook_sessions = param_webhook_sessions,
    integration_data = param_integration_data;
END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getChatStateFromHash`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `getChatStateFromHash`(
  IN `param_chatId_hash` VARCHAR(100))
BEGIN
    SELECT * FROM chatState WHERE chat_id_hash = param_chatId_hash;
  END;;
DELIMITER ;
