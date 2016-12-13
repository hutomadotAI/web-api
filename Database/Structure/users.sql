# Privileges for `admin`@`%`

GRANT USAGE ON *.* TO 'admin'@'%' IDENTIFIED BY PASSWORD '*6BACDD7311879E6121555E6365B7B66D9763E49E';

GRANT ALL PRIVILEGES ON `hutoma`.* TO 'admin'@'%' WITH GRANT OPTION;


# Privileges for `aiDeleter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'aiDeleter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*7EFA4E71AB6B68DCF3EA4291B2151CCF516CDEAE';

GRANT EXECUTE ON `hutoma`.* TO 'aiDeleter'@'127.0.0.1';

GRANT SELECT, DELETE ON `hutoma`.`ai` TO 'aiDeleter'@'127.0.0.1';


# Privileges for `aiReader`@`127.0.0.1`

GRANT USAGE ON *.* TO 'aiReader'@'127.0.0.1' IDENTIFIED BY PASSWORD '*07FCDB827FFB6CBC7971433EADA7B81E7F3E1328';

GRANT EXECUTE ON `hutoma`.* TO 'aiReader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`ai` TO 'aiReader'@'127.0.0.1';


# Privileges for `aiWriter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'aiWriter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*EF8F1009CF338D6FE006551C74F80A24A5E59EB9';

GRANT EXECUTE ON `hutoma`.* TO 'aiWriter'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`ai` TO 'aiWriter'@'127.0.0.1';


# Privileges for `chatlogReader`@`127.0.0.1`

GRANT USAGE ON *.* TO 'chatlogReader'@'127.0.0.1' IDENTIFIED BY PASSWORD '*57A6F03B1B99E90CB68F0572DC0DD96F2D7A913C';

GRANT EXECUTE ON `hutoma`.* TO 'chatlogReader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`chatlog` TO 'chatlogReader'@'127.0.0.1';


# Privileges for `chatlogWriter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'chatlogWriter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*CE7F2778A67D63A07FF11AE78559ADC96A612CFD';

GRANT EXECUTE ON `hutoma`.* TO 'chatlogWriter'@'127.0.0.1';

GRANT INSERT ON `hutoma`.`chatlog` TO 'chatlogWriter'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`ai` TO `chatlogWriter`@`127.0.0.1`;


# Privileges for `debian-sys-maint`@`127.0.0.1`

GRANT ALL PRIVILEGES ON *.* TO 'debian-sys-maint'@'127.0.0.1' IDENTIFIED BY PASSWORD '*E0C3E92BFDAD93DD265D2D527A71F7DE26B211C3' WITH GRANT OPTION;


# Privileges for `entityUser`@`127.0.0.1`

GRANT USAGE ON *.* TO 'entityUser'@'127.0.0.1' IDENTIFIED BY PASSWORD '*DC83C11DF8A0AA5DA890988DD0CF05804F300974';

GRANT EXECUTE ON `hutoma`.* TO 'entityUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`entity_value` TO 'entityUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`entity` TO 'entityUser'@'127.0.0.1';


# Privileges for `hutoma_caller`@`%`

GRANT USAGE ON *.* TO 'hutoma_caller'@'%' IDENTIFIED BY PASSWORD '*EAF232196B73ACE914A99F0607D770FB81BDE348';

GRANT EXECUTE ON `hutoma`.* TO 'hutoma_caller'@'%';


# Privileges for `integrReader`@`127.0.0.1`

GRANT USAGE ON *.* TO 'integrReader'@'127.0.0.1' IDENTIFIED BY PASSWORD '*D4F8BDC0CD9A30E12DDD19CB859B6830A81F0BF2';

GRANT EXECUTE ON `hutoma`.* TO 'integrReader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`integrations` TO 'integrReader'@'127.0.0.1';


# Privileges for `127.0.0.1r`@`127.0.0.1`

GRANT EXECUTE ON *.* TO 'intentUser'@'127.0.0.1' IDENTIFIED BY PASSWORD '*D79A180712799AD35A24608FFDDAEC055ECB7D94';

GRANT EXECUTE ON `hutoma`.* TO 'intentUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`intent_variable_prompt` TO 'intentUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`intent_user_says` TO 'intentUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`intent_variable` TO 'intentUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`intent_response` TO 'intentUser'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`intent` TO 'intentUser'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`ai` TO 'intentUser'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`entity` TO 'intentUser'@'127.0.0.1';


# Privileges for `phpmyadmin`@`127.0.0.1`

GRANT USAGE ON *.* TO 'phpmyadmin'@'127.0.0.1' IDENTIFIED BY PASSWORD '*C520DACC162F4BC391740E48C79814189D4780C7';

GRANT ALL PRIVILEGES ON `phpmyadmin`.* TO 'phpmyadmin'@'127.0.0.1';


# Privileges for `rateLimiter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'rateLimiter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*57A6F03B1B99E90CB68F0572DC0DD96F2D7A913C';

GRANT DELETE, EXECUTE ON `hutoma`.* TO 'rateLimiter'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`api_rate_limit` TO 'rateLimiter'@'127.0.0.1';


# Privileges for `userDeleter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'userDeleter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*DBD50A22424E1DD5EEECDE250B16BF27E5D77D45';

GRANT EXECUTE ON `hutoma`.* TO 'userDeleter'@'127.0.0.1';

GRANT SELECT, DELETE ON `hutoma`.`users` TO 'userDeleter'@'127.0.0.1';


# Privileges for `userTableReader`@`127.0.0.1`

GRANT USAGE ON *.* TO 'userTableReader'@'127.0.0.1' IDENTIFIED BY PASSWORD '*772F1183F2BBB885AE09AAEDCEA59F5FA4137D0D';

GRANT EXECUTE ON `hutoma`.* TO 'userTableReader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`users` TO 'userTableReader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`memoryIntent` TO 'userTableReader'@'127.0.0.1';


# Privileges for `userTableWriter`@`127.0.0.1`

GRANT USAGE ON *.* TO 'userTableWriter'@'127.0.0.1' IDENTIFIED BY PASSWORD '*B9E0112CCFFE4EC5C6CAC0EC435260677A747D95';

GRANT EXECUTE ON `hutoma`.* TO 'userTableWriter'@'127.0.0.1';

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`users` TO 'userTableWriter'@'127.0.0.1';


# Privileges for `mesh_reader`@`127.0.0.1`

GRANT USAGE ON *.* TO 'mesh_reader'@'127.0.0.1' IDENTIFIED BY PASSWORD '*B9E0112CCFFE4EC5C6CAC0EC435260677A747D95';

GRANT EXECUTE ON `hutoma`.* TO 'mesh_reader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`ai_mesh` TO 'mesh_reader'@'127.0.0.1';

GRANT SELECT ON `hutoma`.`ai` TO 'mesh_reader'@'127.0.0.1';


# Privileges for `mesh_writer`@`127.0.0.1`

GRANT USAGE ON *.* TO 'mesh_writer'@'127.0.0.1' IDENTIFIED BY PASSWORD '*B9E0112CCFFE4EC5C6CAC0EC435260677A747D95';

GRANT EXECUTE ON `hutoma`.* TO 'mesh_writer'@'127.0.0.1';

GRANT INSERT, UPDATE, DELETE ON `hutoma`.`ai_mesh` TO 'mesh_writer'@'127.0.0.1';