/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

# Support for Django to talk to existing MySql DB
# Define `django_caller`@`%`
GRANT USAGE ON *.* TO 'django_caller'@'%' IDENTIFIED BY PASSWORD '*43AB6D5047308CDDD3C9C7BF244A184EB22559E2';
# Allow Django to create new tables
GRANT CREATE, EXECUTE ON `hutoma`.* TO 'django_caller'@'%';

# Allow Django to manipulate users table
GRANT SELECT, INSERT, UPDATE, ALTER ON `hutoma`.`users` TO 'django_caller'@'%';
# Allow Django to manage the following tables it creates
GRANT ALL ON `hutoma`.`account_emailaddress` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`account_emailconfirmation` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_group` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_group_permissions` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_permission` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_user` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_user_groups` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`auth_user_user_permissions` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`django_admin_log` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`django_content_type` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`django_migrations` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`django_session` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`django_site` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`reversion_revision` TO 'django_caller'@'%';
GRANT ALL ON `hutoma`.`reversion_version` TO 'django_caller'@'%';
