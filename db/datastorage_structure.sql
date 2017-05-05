-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: data_storage
-- ------------------------------------------------------
-- Server version	5.7.18-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flexi_multiradio_bts_lte_alarms`
--

DROP TABLE IF EXISTS `flexi_multiradio_bts_lte_alarms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flexi_multiradio_bts_lte_alarms` (
  `Changes between issues 03A and 03B` text,
  `Changes between releases FDD-LTE 15A and FDD-LTE 16` text,
  `Reason for Change/Removal` text,
  `Alarm Number` int(11) NOT NULL,
  `Alarm Version` text,
  `Alarm Name` text,
  `Alarm Name in issue 03A` text,
  `Alarm Name in release FDD-LTE 15A` text,
  `Probable Cause` text,
  `Probable Cause in issue 03A` text,
  `Probable Cause in release FDD-LTE 15A` text,
  `Event Type` text,
  `Event Type in issue 03A` text,
  `Event Type in release FDD-LTE 15A` text,
  `Default Severity` text,
  `Default Severity in issue 03A` text,
  `Default Severity in release FDD-LTE 15A` text,
  `Meaning` text,
  `Meaning in issue 03A` text,
  `Meaning in release FDD-LTE 15A` text,
  `Effect` text,
  `Effect in issue 03A` text,
  `Effect in release FDD-LTE 15A` text,
  `Identifying Additional Information Fields` text,
  `Identifying Additional Information Fields in issue 03A` text,
  `Identifying Additional Information Fields in release FDD-LTE 15A` text,
  `Additional Information Fields` text,
  `Additional Information Fields in issue 03A` text,
  `Additional Information Fields in release FDD-LTE 15A` text,
  `Instructions` text,
  `Instructions in issue 03A` text,
  `Instructions in release FDD-LTE 15A` text,
  `Related Faults` text,
  `Related Faults in issue 03A` text,
  `Related Faults in release FDD-LTE 15A` text,
  `Clearing` text,
  `Clearing in issue 03A` text,
  `Clearing in release FDD-LTE 15A` text,
  `Time to Live` text,
  `Time to Live in issue 03A` text,
  `Time to Live in release FDD-LTE 15A` text,
  `Used in Product` text,
  `Detecting Unit` text,
  `Source` text,
  `State` text,
  `Unit Status` text,
  `Unit Status Attributes` text,
  `Reported Alarms` text,
  `Reported Alarms Description` text,
  `Detecting Unit in issue 03A` text,
  `Detecting Unit in release FDD-LTE 15A` text,
  `Source in issue 03A` text,
  `Source in release FDD-LTE 15A` text,
  `State in issue 03A` text,
  `State in release FDD-LTE 15A` text,
  `Unit Status in issue 03A` text,
  `Unit Status in release FDD-LTE 15A` text,
  `Unit Status Attributes in issue 03A` text,
  `Unit Status Attributes in release FDD-LTE 15A` text,
  `Reported Alarms in issue 03A` text,
  `Reported Alarms in release FDD-LTE 15A` text,
  `Reported Alarms Description in issue 03A` text,
  `Reported Alarms Description in release FDD-LTE 15A` text,
  `Fault Name in issue 03A` text,
  `Fault Name in release FDD-LTE 15A` text,
  PRIMARY KEY (`Alarm Number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flexi_multiradio_bts_lte_parameters`
--

DROP TABLE IF EXISTS `flexi_multiradio_bts_lte_parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flexi_multiradio_bts_lte_parameters` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `Changes between issues 05A and 05B` text,
  `Changes between releases FDD-LTE15A and FDD-LTE16` text,
  `Abbreviated Name` text,
  `MO Class` text,
  `Parameter Category` text,
  `Parent Structure` text,
  `Parent Structure in issue 05A` text,
  `Parent Structure in release FDD-LTE15A` text,
  `Child Parameters` text,
  `Child Parameters in issue 05A` text,
  `Child Parameters in release FDD-LTE15A` text,
  `Full Name` text,
  `Full Name in issue 05A` text,
  `Full Name in release FDD-LTE15A` text,
  `3GPP Name` text,
  `3GPP Name in issue 05A` text,
  `3GPP Name in release FDD-LTE15A` text,
  `Data Type` text,
  `Classification` text,
  `Classification in issue 05A` text,
  `Classification in release FDD-LTE15A` text,
  `Multiplicity` text,
  `Multiplicity in issue 05A` text,
  `Multiplicity in release FDD-LTE15A` text,
  `Description` text,
  `Description in issue 05A` text,
  `Description in release FDD-LTE15A` text,
  `Range and step` mediumtext,
  `Formula for Getting Internal Value` text,
  `Default Value` text,
  `Default Value Notes` text,
  `Default Value Notes in issue 05A` text,
  `Default Value Notes in release FDD-LTE15A` text,
  `Related Functions` text,
  `Related Functions in issue 05A` text,
  `Related Functions in release FDD-LTE15A` text,
  `Modification` text,
  `Modification in issue 05A` text,
  `Modification in release FDD-LTE15A` text,
  `Required on Creation` text,
  `Required on Creation in issue 05A` text,
  `Required on Creation in release FDD-LTE15A` text,
  `Related Parameters` text,
  `Related Parameters in issue 05A` text,
  `Related Parameters in release FDD-LTE15A` text,
  `Parameter Relationships` text,
  `Parameter Relationships in issue 05A` text,
  `Parameter Relationships in release FDD-LTE15A` text,
  `Related Features` text,
  `Related Features in issue 05A` text,
  `Related Features in release FDD-LTE15A` text,
  `Interfaces` text,
  `Interfaces in issue 05A` text,
  `Interfaces in release FDD-LTE15A` text,
  `References` text,
  `References in issue 05A` text,
  `References in release FDD-LTE15A` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2967 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_mapping`
--

DROP TABLE IF EXISTS `intent_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_mapping` (
  `intent_name` varchar(255) NOT NULL,
  `data_table` varchar(100) DEFAULT NULL,
  `key_column` varchar(100) DEFAULT NULL,
  `key_entity` varchar(100) DEFAULT NULL,
  `value_entity` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`intent_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'data_storage'
--

--
-- Dumping routines for database 'data_storage'
--
/*!50003 DROP PROCEDURE IF EXISTS `getData` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`dataReader`@`127.0.0.1` PROCEDURE `getData`(
  IN `in_data_table` VARCHAR(100),
  IN `in_key_column` VARCHAR(100),
  IN `in_key_entity` VARCHAR(100),
  IN `in_value_column` VARCHAR(100))
    READS SQL DATA
BEGIN
    SET @QueryStr = CONCAT("SELECT `",`in_value_column`,"` FROM ",`in_data_table`," WHERE `",`in_key_column`,"`='",`in_key_entity`,"'");
    PREPARE Query FROM @QueryStr;
    EXECUTE Query;
    DEALLOCATE PREPARE Query;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentMapping` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`intentMappingReader`@`127.0.0.1` PROCEDURE `getIntentMapping`(
  IN `in_intent_name` VARCHAR(255))
    READS SQL DATA
BEGIN

    SELECT
       *
       FROM `intent_mapping`
       WHERE `intent_mapping`.`intent_name`=`in_intent_name`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-05-01 11:15:22
