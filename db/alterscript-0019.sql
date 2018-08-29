USE hutoma;

DROP PROCEDURE `getAIsForEntity`;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getAIsForEntity`(
    IN `param_devid` VARCHAR(50),
    IN `param_entity_name` VARCHAR(50)
) READS SQL DATA
BEGIN

    SELECT DISTINCT `ai`.`aiid`
    FROM `intent` i INNER JOIN `ai` on i.`aiid` = `ai`.`aiid`
    WHERE JSON_CONTAINS(JSON_EXTRACT(`intent_json`, "$.variables[*].entity_name"), JSON_ARRAY(param_entity_name))
      AND `ai`.`dev_id`= param_devid;

  END;;
DELIMITER ;
