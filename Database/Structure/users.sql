# Privileges for `admin`@`%`

GRANT USAGE ON *.* TO 'admin'@'%' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'%' WITH GRANT OPTION;


# Privileges for `admin`@`127.0.0.1`

GRANT USAGE ON *.* TO 'admin'@'127.0.0.1' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'127.0.0.1' WITH GRANT OPTION;


# Privileges for `admin`@`localhost`

GRANT USAGE ON *.* TO 'admin'@'localhost' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'localhost' WITH GRANT OPTION;


# Privileges for `debian-sys-maint`@`localhost`

GRANT ALL PRIVILEGES ON *.* TO 'debian-sys-maint'@'localhost' IDENTIFIED BY PASSWORD '*E0C3E92BFDAD93DD265D2D527A71F7DE26B211C3' WITH GRANT OPTION;


# Privileges for `domainsReader`@`localhost`

GRANT USAGE ON *.* TO 'domainsReader'@'localhost' IDENTIFIED BY PASSWORD '*D62B70835DDC6B34883C18DEC1CF0C5006DDA875';

GRANT EXECUTE ON `hutoma`.* TO 'domainsReader'@'localhost';

GRANT SELECT ON `hutoma`.`domains` TO 'domainsReader'@'localhost';

GRANT SELECT ON `hutoma`.`userAIDomains` TO 'domainsReader'@'localhost';


# Privileges for `domainsWriter`@`localhost`

GRANT USAGE ON *.* TO 'domainsWriter'@'localhost' IDENTIFIED BY PASSWORD '*79415A01A07F95E07C49BEDE6FD0113A707A3C67';

GRANT EXECUTE ON `hutoma`.* TO 'domainsWriter'@'localhost';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`userAIDomains` TO 'domainsWriter'@'localhost';


# Privileges for `hutoma_caller`@`%`

GRANT USAGE ON *.* TO 'hutoma_caller'@'%' IDENTIFIED BY PASSWORD '*EAF232196B73ACE914A99F0607D770FB81BDE348';

GRANT EXECUTE ON `hutoma`.* TO 'hutoma_caller'@'%';


# Privileges for `integrReader`@`localhost`

GRANT USAGE ON *.* TO 'integrReader'@'localhost' IDENTIFIED BY PASSWORD '*D4F8BDC0CD9A30E12DDD19CB859B6830A81F0BF2';

GRANT EXECUTE ON `hutoma`.* TO 'integrReader'@'localhost';

GRANT SELECT ON `hutoma`.`integrations` TO 'integrReader'@'localhost';


# Privileges for `phpmyadmin`@`localhost`

GRANT USAGE ON *.* TO 'phpmyadmin'@'localhost' IDENTIFIED BY PASSWORD '*C520DACC162F4BC391740E48C79814189D4780C7';

GRANT ALL PRIVILEGES ON `phpmyadmin`.* TO 'phpmyadmin'@'localhost';


# Privileges for `root`@`%`

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `mysql`.* TO 'root'@'%';


# Privileges for `root`@`127.0.0.1`

GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E' WITH GRANT OPTION;


# Privileges for `root`@`::1`

GRANT ALL PRIVILEGES ON *.* TO 'root'@'::1' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E' WITH GRANT OPTION;


# Privileges for `root`@`localhost`

GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E' WITH GRANT OPTION;

GRANT PROXY ON ''@'' TO 'root'@'localhost' WITH GRANT OPTION;


# Privileges for `userTableReader`@`localhost`

GRANT USAGE ON *.* TO 'userTableReader'@'localhost' IDENTIFIED BY PASSWORD '*772F1183F2BBB885AE09AAEDCEA59F5FA4137D0D';

GRANT EXECUTE ON `hutoma`.* TO 'userTableReader'@'localhost';

GRANT SELECT ON `hutoma`.`users` TO 'userTableReader'@'localhost';


# Privileges for `userTableWriter`@`localhost`

GRANT USAGE ON *.* TO 'userTableWriter'@'localhost' IDENTIFIED BY PASSWORD '*B9E0112CCFFE4EC5C6CAC0EC435260677A747D95';

GRANT EXECUTE ON `hutoma`.* TO 'userTableWriter'@'localhost';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`users` TO 'userTableWriter'@'localhost';