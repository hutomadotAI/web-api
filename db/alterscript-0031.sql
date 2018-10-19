USE hutoma;

DROP PROCEDURE IF EXISTS `getEntityValuesCountForDevExcludingEntity`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityValuesCountForDevExcludingEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT COUNT(*) as COUNT
  FROM `entity` e INNER JOIN `entity_value` ev ON ev.`entity_id`=e.`id` 
  WHERE e.`dev_id`=in_dev_id AND e.`name`<>in_name;
END;;
DELIMITER ;