FROM php:7-apache
LABEL maintainer "pedrotei@hutoma.com"

ARG DEBUG=0

RUN apt-get update && apt-get -y upgrade && DEBIAN_FRONTEND=noninteractive apt-get -y install \
    libcurl4-gnutls-dev \
    curl \
    lynx-cur  \
    wget


# Install PHP's curl
RUN /usr/local/bin/docker-php-ext-install curl json
    
# Copy the PHP configuration
COPY config/php.ini /usr/local/etc/php/php.ini


# Copy the site and server configuration
COPY config/000-default.conf /etc/apache2/sites-enabled
COPY config/apache2.conf /etc/apache2/

# Install xDebug if in debug mode
RUN if [ $DEBUG -eq 1 ]; then pecl install xdebug \
    && echo "zend_extension=$(find /usr/local/lib/php/extensions/ -name xdebug.so)" > /usr/local/etc/php/conf.d/xdebug.ini \
    && echo "xdebug.remote_enable=on" >> /usr/local/etc/php/conf.d/xdebug.ini \
    && echo "xdebug.remote_autostart=off" >> /usr/local/etc/php/conf.d/xdebug.ini; fi

# Copy the dev console code to the web root folder
COPY src /var/www/html

# Expose default ports for HTTP and HTTPS
EXPOSE 80

# Expose environment variables for the console
ENV HUTOMA_API_URL http://server/v1
