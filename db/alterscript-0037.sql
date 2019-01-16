USE `hutoma`;

DROP PROCEDURE IF EXISTS `hutoma`.`deleteEntity`;

DELIMITER $$
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntityByName`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_entity_name VARCHAR(250))
BEGIN
    DELETE FROM `entity` 
    WHERE `in_dev_id`=`dev_id`
		AND `in_aiid`=`aiid`
        AND `in_entity_name`=`name`;
END$$
DELIMITER ;

