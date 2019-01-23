FROM mysql:5.7.25
LABEL maintainer  "Pedro Teixeira<pedrotei@hutoma.com>"

# Configuration files
COPY mysql.cnf /etc/mysql/conf.d/mysql.cnf

# API DB
COPY structure.sql /docker-entrypoint-initdb.d/0.sql
COPY users.sql /docker-entrypoint-initdb.d/1.sql
COPY default-data.sql /docker-entrypoint-initdb.d/2.sql

# Django database
COPY django_structure.sql /docker-entrypoint-initdb.d/3.sql

# Test data - should not be added to production
COPY test-data.sql /docker-entrypoint-initdb.d/4.sql

# Copy all existing alterscripts
COPY alterscript-*.sql /docker-entrypoint-initdb.d/

# Default values for passwords and database name. Can be overridden on docker run
ENV MYSQL_ROOT_PASSWORD=password

# -- FOR LOCAL TESTING ONLY, to allow binding to all interfaces (0.0.0.0)
RUN sed -Ei 's/^(bind-address|log)/#&/' /etc/mysql/my.cnf


EXPOSE 3306