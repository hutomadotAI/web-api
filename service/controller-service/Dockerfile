FROM tomcat:8.5.37-jre8-slim
LABEL maintainer "pedrotei@hutoma.com"

# Copy the server config
COPY config/server.xml /usr/local/tomcat/conf

# Delete any existing root folder
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Add a tomcat user
ARG USERID=1000
ARG GROUPID=1000
RUN addgroup --system --gid $GROUPID tomcat
RUN adduser --system --uid $USERID --gid $GROUPID tomcat
RUN chown -R tomcat:tomcat /usr/local/tomcat/
USER tomcat

# Copy the WAR file to be expanded as the new ROOT folder (as we have set unpackWARs="true")
COPY ./target/Controller.war /usr/local/tomcat/webapps/ROOT.war

# Expose the following environment variables
ENV API_CONNECTION_STRING jdbc:mysql://server:3306/hutoma?user=user&password=password&zeroDateTimeBehavior=convertToNull
ENV API_DBCONNECTIONPOOL_MIN_SIZE 8
ENV API_DBCONNECTIONPOOL_MAX_SIZE 256
ENV API_LOGGING_FLUENT_HOST log-fluent
ENV API_LOGGING_FLUENT_PORT 24224


EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]