FROM php:7-apache
LABEL maintainer "pedrotei@hutoma.com"

RUN \
	apt-get update && apt-get -y upgrade && DEBIAN_FRONTEND=noninteractive apt-get -y install \
    libcurl4-gnutls-dev curl lynx-cur wget


# Install PHP's curl
RUN \
  	/usr/local/bin/docker-php-ext-install curl json
  	
# Enable apache SSL module
RUN \
	a2enmod ssl

# Create the folder to hold the certificates
RUN \
	mkdir -p /etc/apache2/ssl

# Copy the PHP configuration
COPY config/php.ini /usr/local/etc/php/php.ini

# Copy the certificates to the store
COPY config/internal.hutoma.crt /etc/apache2/ssl
COPY config/internal.hutoma.key /etc/apache2/ssl

# Copy the site and server configuration
COPY config/000-default.conf /etc/apache2/sites-enabled
COPY config/default-ssl.conf /etc/apache2/sites-enabled
COPY config/apache2.conf /etc/apache2/


# Copy the dev console code to the web root folder
COPY src /var/www/html

# Expose default ports for HTTP and HTTPS
EXPOSE 80 443

# Expose environment variables for the console
ENV HUTOMA_API_URL http://server/v1
