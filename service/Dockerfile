FROM tomcat:8-jre8
LABEL maintainer "pedrotei@hutoma.com"

# Copy the certificates to the store
RUN mkdir -p /usr/local/tomcat/ssl
COPY config/internal.hutoma.crt /usr/local/tomcat/ssl
COPY config/internal.hutoma.key /usr/local/tomcat/ssl

# Copy the server config
COPY config/server.xml /usr/local/tomcat/conf

# Delete any existing root folder
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copy the WAR file to be expanded as the new ROOT folder (as we have set unpackWARs="true")
COPY ./target/HutomaAPI.war /usr/local/tomcat/webapps/ROOT.war

# Expose the following environment variables
ENV API_ENCODING_KEY encodingKey
ENV API_MAX_UPLOAD_SIZE_KB 16384
ENV API_CONNECTION_STRING jdbc:mysql://server:3306/hutoma?user=user&password=password&zeroDateTimeBehavior=convertToNull
ENV API_DBCONNECTIONPOOL_MIN_SIZE 8
ENV API_DBCONNECTIONPOOL_MAX_SIZE 64
ENV API_RATELIMIT_CHAT_BURST 3.0
ENV API_RATELIMIT_CHAT_FREQUENCY 2.0
ENV API_RATELIMIT_QUICKREAD_BURST 7.0
ENV API_RATELIMIT_QUICKREAD_FREQUENCY 0.5
ENV API_RATELIMIT_LOADTEST_BURST 32768.0
ENV API_RATELIMIT_LOADTEST_FREQUENCY 8192.5
ENV API_BACKEND_REQUEST_TIMEOUT_MS 20000
ENV API_LOGGING_URL http://log-svc:8080/log
ENV API_LOGGING_CADENCY 5000

# Expose bothe the secure and non-secure ports
EXPOSE 8080 8443

# Start Tomcat
CMD ["catalina.sh", "run"]