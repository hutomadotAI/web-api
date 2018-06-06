USE `hutoma`;

DROP TABLE IF EXISTS `migration_status`;
CREATE TABLE `migration_status`  (
  `enforce_one_row` enum('only') not null unique default 'only',
  `migration_date` date NOT NULL,
  `migration_id` int(11) NOT NULL,
  PRIMARY KEY (`enforce_one_row`)
) ENGINE=InnoDB;

INSERT INTO migration_status (enforce_one_row, migration_date, migration_id)
  VALUES ('only', '2018-06-06', 9);