/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

DROP PROCEDURE IF EXISTS `getWebhookSecretForBot`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getWebhookSecretForBot`(IN `param_aiid` VARCHAR(50))
BEGIN
    SELECT `client_token` FROM `ai` WHERE `aiid` = `param_aiid`;
END ;;
DELIMITER ;

