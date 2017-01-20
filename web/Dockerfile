FROM php:7-apache
MAINTAINER  Pedro Teixeira pedrotei@hutoma.com

RUN \
  apt-get update \
  && apt-get install -y libcurl4-gnutls-dev curl wget


RUN \
    /usr/local/bin/docker-php-ext-install curl json mysqli pdo pdo_mysql

COPY src /var/www/html

ENV HUTOMA_API_DB_CONNECTION_STRING mysql:dbname=hutoma;host=localhost;port=3306;charset=utf8
ENV HUTOMA_API_DB_USERNAME username
ENV HUTOMA_API_DB_PASSWORD password
ENV HUTOMA_API_URL http://server/v1


EXPOSE 80