FROM mysql:5.7
MAINTAINER  Pedro Teixeira pedrotei@hutoma.com


COPY Structure/StructureDump.sql /docker-entrypoint-initdb.d
COPY Structure/users.sql /docker-entrypoint-initdb.d

# Default values for passwords and database name. Can be overridden on docker run
ENV MYSQL_ROOT_PASSWORD=password

# -- FOR LOCAL TESTING ONLY, to allow binding to all interfaces (0.0.0.0)
RUN sed -Ei 's/^(bind-address|log)/#&/' /etc/mysql/my.cnf


EXPOSE 3306