USE hutoma;

GRANT SELECT ON `hutoma`.`intent` TO 'aiReader'@'127.0.0.1';

DROP PROCEDURE IF EXISTS `getAisForRetraining`;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAisForRetraining`()
BEGIN
	SELECT ai.id, ai.aiid, ai.dev_id, botStore.publishing_state
    FROM ai LEFT JOIN botStore ON ai.aiid=botStore.aiid
    WHERE ai.deleted='0' AND 
	  ai.passthrough_url IS NULL AND 
	  (ai.aiid IN(SELECT ai_training.aiid FROM ai_training) 
         OR ai.aiid IN(SELECT intent.aiid FROM intent));
END;;
DELIMITER ;
