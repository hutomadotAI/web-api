/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `ai` ADD COLUMN hmac_secret varchar(50)  DEFAULT NULL AFTER `client_token`;

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