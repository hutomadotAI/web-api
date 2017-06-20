/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `botStore` ADD COLUMN featured tinyint(1) NOT NULL DEFAULT 0 AFTER `botIcon`;


DROP PROCEDURE IF EXISTS `getAisLinkedToAi`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAisLinkedToAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50))
BEGIN
	SELECT bai.aiid as 'ai', bs.aiid as 'linked_ai', bai.dev_id as 'linked_ai_devId', ai.ui_ai_confidence as 'minP'
	FROM bot_ai bai INNER JOIN botStore bs ON bs.id = bai.botId INNER JOIN ai ai ON ai.aiid = bs.aiid
	WHERE bai.aiid=param_aiid AND bai.dev_id=param_devId;
END ;;
DELIMITER ;
