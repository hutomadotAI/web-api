FROM php:7-apache
LABEL maintainer "pedrotei@hutoma.com"

# Create /etc/mailname for postfix
RUN \
	echo hutoma.com > /etc/mailname

RUN \
	apt-get update && apt-get -y upgrade && DEBIAN_FRONTEND=noninteractive apt-get -y install \
    libcurl4-gnutls-dev curl lynx-cur wget libsasl2-modules postfix mailutils


# Install PHP's curl, PDO and mysql connection stuff
RUN \
  	/usr/local/bin/docker-php-ext-install curl json mysqli pdo pdo_mysql
  	
# Enable apache SSL module
RUN \
	a2enmod ssl

# Create the folder to hold the certificates
RUN \
	mkdir -p /etc/apache2/ssl

# Copy the certificates to the store
COPY config/internal.hutoma.crt /etc/apache2/ssl
COPY config/internal.hutoma.key /etc/apache2/ssl

# Copy the site and server configuration
COPY config/000-default.conf /etc/apache2/sites-enabled
COPY config/default-ssl.conf /etc/apache2/sites-enabled
COPY config/apache2.conf /etc/apache2/

# Copy the postfix configuration to enable sending out emails
COPY config/postfix-main.cf /etc/postfix/main.cf
COPY config/sasl_passwd.db /etc/postfix/sasl_passwd.db

# Copy the name resolution file for postfix to use it (otherwise it won't
# be able to resolve the relay service address)
RUN \
	cp /etc/resolv.conf /var/spool/postfix/etc/resolv.conf

# Copy the dev console code to the web root folder
COPY src /var/www/html

# Expose default ports for HTTP and HTTPS
EXPOSE 80 443

# Expose environment variables for the console
ENV HUTOMA_API_DB_CONNECTION_STRING mysql:dbname=hutoma;host=localhost;port=3306;charset=utf8
ENV HUTOMA_API_DB_USERNAME username
ENV HUTOMA_API_DB_PASSWORD password
ENV HUTOMA_API_URL http://server/v1

# Copy the script that launches all the required services
COPY config/start_services.sh /usr/local/bin/
# And run it
CMD ["start_services.sh"]
