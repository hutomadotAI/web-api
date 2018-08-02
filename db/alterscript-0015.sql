USE hutoma;

DROP PROCEDURE `getPublishedBots`;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getPublishedBots`(
IN `param_publishing_type` TINYINT(1))
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE publishing_state = 2 AND publishing_type = param_publishing_type;
  END;;
DELIMITER ;


DROP PROCEDURE `getBotstoreListPerCategory`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreListPerCategory`(
    IN `param_max` INT(11))
BEGIN
    SET @select_query =
    "SELECT * FROM(
        SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website',
            (@num:=if(@group = bs.category, @num +1, if(@group := bs.category, 1, 1))) row_number
        FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id
        CROSS JOIN (select @num:=0, @group:=null) c
        WHERE publishing_state=2 AND publishing_type=1
        ORDER BY bs.category
    ) as x
    WHERE x.row_number <= ";

    SET @limitTo = param_max;

    SET @query = concat(@select_query, @limitTo);

    PREPARE stmt3 FROM @query;
    EXECUTE stmt3;
    DEALLOCATE PREPARE stmt3;

END;;
DELIMITER ;

DROP PROCEDURE `getBotstoreList`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreList`(
    IN `param_filters` VARCHAR(255),
    IN `param_order` VARCHAR(50),
    IN `param_pageStart` INT,
    IN `param_pageSize` INT)
BEGIN

SET @select_query = "SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website' ";
SET @from_query = "FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id WHERE publishing_state=2 AND publishing_type=1 ";
IF LENGTH(param_filters) = 0 THEN
    SET @where_other = "";
ELSE
    SET @where_other = concat(" AND ", param_filters);
END IF;
IF LENGTH(param_order) = 0 THEN
    SET @orderBy = "";
ELSE
    SET @orderBy = concat(" ORDER BY ", param_order);
END IF;
SET @limitTo = concat(concat(concat(" LIMIT ", param_pageStart), ", "), param_pageSize);
SET @query = concat(concat(concat(concat(@select_query, @from_query), @where_other), @orderBy), @limitTo);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;


SET @select_query = "SELECT COUNT(*) as 'total'";
SET @query = concat(concat(@select_query, @from_query), @where_other);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

END;;
DELIMITER ;