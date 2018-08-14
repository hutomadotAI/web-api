USE hutoma;

CREATE TABLE IF NOT EXISTS `feature_toggle`  (
  `devid` VARCHAR(50) NULL DEFAULT NULL,
  `aiid` VARCHAR(50) NULL DEFAULT NULL,
  `feature` VARCHAR(50) NOT NULL,
  `state` VARCHAR(3) NOT NULL
) ENGINE=InnoDB;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getFeatures`()
BEGIN
    SELECT `devid`, `aiid`, `feature`, `state`
    FROM `feature_toggle`;
END;;
DELIMITER ;


GRANT SELECT ON `hutoma`.`feature_toggle` TO 'aiReader'@'127.0.0.1';