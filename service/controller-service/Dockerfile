FROM tomcat:8.5.23-jre8
LABEL maintainer "pedrotei@hutoma.com"

# Delete any existing root folder
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copy the server config
COPY config/server.xml /usr/local/tomcat/conf

# Copy the WAR file to be expanded as the new ROOT folder (as we have set unpackWARs="true")
COPY ./target/Controller.war /usr/local/tomcat/webapps/ROOT.war

# Expose the following environment variables
ENV API_CONNECTION_STRING jdbc:mysql://server:3306/hutoma?user=user&password=password&zeroDateTimeBehavior=convertToNull
ENV API_DBCONNECTIONPOOL_MIN_SIZE 8
ENV API_DBCONNECTIONPOOL_MAX_SIZE 64
ENV API_LOGGING_FLUENT_HOST log-fluent
ENV API_LOGGING_FLUENT_PORT 24224


EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]