FROM dordoka/tomcat
LABEL maintainer "pedrotei@hutoma.com"

# Copy the certificates to the store
RUN mkdir -p /usr/local/tomcat/ssl
COPY config/internal.hutoma.crt /usr/local/tomcat/ssl
COPY config/internal.hutoma.key /usr/local/tomcat/ssl

# Copy the server config
COPY config/server.xml /usr/local/tomcat/conf

# Delete any existing root folder
RUN rm -rf /opt/tomcat/webapps/ROOT
# Copy the WAR file to be expanded as the new ROOT folder (as we have set unpackWARs="true")
COPY ./target/HutomaAPI.war /opt/tomcat/webapps/ROOT.war

# Expose the following environment variables
ENV API_ENCODING_KEY encodingKey
ENV API_MAX_UPLOAD_SIZE_KB 512
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
ENV API_AI_AIML_BOT_AIIDS e1bb8226-e8ce-467a-8305-bc2fcb89dd7f,491091b1-5458-4d87-b7e9-0ccf3002970c,9a7a3376-e872-463e-aaa5-9f7e0e8c3380,873e61b9-9b75-449b-9532-821fcea6f864

# Expose bothe the secure and non-secure ports
EXPOSE 8080 8443 5005 7012

# Start Tomcat
CMD ["catalina.sh", "run"]