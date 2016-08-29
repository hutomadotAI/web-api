# Privileges for `admin`@`%`

GRANT USAGE ON *.* TO 'admin'@'%' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'%' WITH GRANT OPTION;


# Privileges for `admin`@`127.0.0.1`

GRANT USAGE ON *.* TO 'admin'@'127.0.0.1' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'127.0.0.1' WITH GRANT OPTION;


# Privileges for `admin`@`localhost`

GRANT USAGE ON *.* TO 'admin'@'localhost' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'localhost' WITH GRANT OPTION;


# Privileges for `aiDeleter`@`localhost`

GRANT USAGE ON *.* TO 'aiDeleter'@'localhost' IDENTIFIED BY PASSWORD '*7EFA4E71AB6B68DCF3EA4291B2151CCF516CDEAE';

GRANT EXECUTE ON `hutoma`.* TO 'aiDeleter'@'localhost';

GRANT SELECT, DELETE ON `hutoma`.`ai` TO 'aiDeleter'@'localhost';


# Privileges for `aiReader`@`localhost`

GRANT USAGE ON *.* TO 'aiReader'@'localhost' IDENTIFIED BY PASSWORD '*07FCDB827FFB6CBC7971433EADA7B81E7F3E1328';

GRANT EXECUTE ON `hutoma`.* TO 'aiReader'@'localhost';

GRANT SELECT ON `hutoma`.`ai` TO 'aiReader'@'localhost';


# Privileges for `aiWriter`@`localhost`

GRANT USAGE ON *.* TO 'aiWriter'@'localhost' IDENTIFIED BY PASSWORD '*EF8F1009CF338D6FE006551C74F80A24A5E59EB9';

GRANT EXECUTE ON `hutoma`.* TO 'aiWriter'@'localhost';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`ai` TO 'aiWriter'@'localhost';


# Privileges for `chatlogReader`@`localhost`

GRANT USAGE ON *.* TO 'chatlogReader'@'localhost' IDENTIFIED BY PASSWORD '*57A6F03B1B99E90CB68F0572DC0DD96F2D7A913C';

GRANT EXECUTE ON `hutoma`.* TO 'chatlogReader'@'localhost';

GRANT SELECT ON `hutoma`.`chatlog` TO 'chatlogReader'@'localhost';


# Privileges for `chatlogWriter`@`localhost`

GRANT USAGE ON *.* TO 'chatlogWriter'@'localhost' IDENTIFIED BY PASSWORD '*CE7F2778A67D63A07FF11AE78559ADC96A612CFD';

GRANT EXECUTE ON `hutoma`.* TO 'chatlogWriter'@'localhost';

GRANT INSERT ON `hutoma`.`chatlog` TO 'chatlogWriter'@'localhost';


# Privileges for `debian-sys-maint`@`localhost`

GRANT ALL PRIVILEGES ON *.* TO 'debian-sys-maint'@'localhost' IDENTIFIED BY PASSWORD '*E0C3E92BFDAD93DD265D2D527A71F7DE26B211C3' WITH GRANT OPTION;


# Privileges for `domainsReader`@`localhost`

GRANT USAGE ON *.* TO 'domainsReader'@'localhost' IDENTIFIED BY PASSWORD '*D62B70835DDC6B34883C18DEC1CF0C5006DDA875';

GRANT EXECUTE ON `hutoma`.* TO 'domainsReader'@'localhost';

GRANT SELECT ON `hutoma`.`userAIDomains` TO 'domainsReader'@'localhost';

GRANT SELECT ON `hutoma`.`domains` TO 'domainsReader'@'localhost';


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


# Privileges for `userDeleter`@`localhost`

GRANT USAGE ON *.* TO 'userDeleter'@'localhost' IDENTIFIED BY PASSWORD '*DBD50A22424E1DD5EEECDE250B16BF27E5D77D45';

GRANT EXECUTE ON `hutoma`.* TO 'userDeleter'@'localhost';

GRANT SELECT, DELETE ON `hutoma`.`users` TO 'userDeleter'@'localhost';


# Privileges for `userTableReader`@`localhost`

GRANT USAGE ON *.* TO 'userTableReader'@'localhost' IDENTIFIED BY PASSWORD '*772F1183F2BBB885AE09AAEDCEA59F5FA4137D0D';

GRANT EXECUTE ON `hutoma`.* TO 'userTableReader'@'localhost';

GRANT SELECT ON `hutoma`.`users` TO 'userTableReader'@'localhost';


# Privileges for `userTableWriter`@`localhost`

GRANT USAGE ON *.* TO 'userTableWriter'@'localhost' IDENTIFIED BY PASSWORD '*B9E0112CCFFE4EC5C6CAC0EC435260677A747D95';

GRANT EXECUTE ON `hutoma`.* TO 'userTableWriter'@'localhost';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`users` TO 'userTableWriter'@'localhost';