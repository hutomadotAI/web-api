FROM mysql:5.7
MAINTAINER  Pedro Teixeira pedrotei@hutoma.com


COPY structure.sql /docker-entrypoint-initdb.d/0.sql
COPY users.sql /docker-entrypoint-initdb.d/1.sql
COPY default-data.sql /docker-entrypoint-initdb.d/2.sql

# Default values for passwords and database name. Can be overridden on docker run
ENV MYSQL_ROOT_PASSWORD=password

# -- FOR LOCAL TESTING ONLY, to allow binding to all interfaces (0.0.0.0)
RUN sed -Ei 's/^(bind-address|log)/#&/' /etc/mysql/my.cnf


EXPOSE 3306