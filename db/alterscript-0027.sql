USE hutoma;

DROP PROCEDURE IF EXISTS `getAllIntentsForDev`;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getAllIntentsForDev`(
  IN `in_dev_id` VARCHAR(50)
  )
BEGIN
	SELECT i.* 
	FROM `intent` i INNER JOIN `ai` ai ON i.aiid = ai.aiid 
	WHERE ai.dev_id = `in_dev_id`
		AND ai.deleted = 0;

END ;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `deleteEntity`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_entity_id int(11))
BEGIN
    DELETE FROM `entity` 
    WHERE `in_dev_id`=`dev_id` 
    	AND `in_entity_id`=`id`;
      
END;;
DELIMITER ;