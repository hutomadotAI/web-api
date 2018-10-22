USE hutoma;

DROP PROCEDURE IF EXISTS `hutoma`.`getBotConfigDefinition`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `hutoma`.`getBotConfigDefinition`(
	IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT api_keys_desc FROM ai WHERE aiid = param_aiid AND dev_id = param_devId;
END;;
DELIMITER ;
