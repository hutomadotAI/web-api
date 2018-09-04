# Local testing of API components

docker-compose of

- api-db
- api-svc
- api-ctrl

A dummy chat backend or two.
An entity recognizer service.

Data stores for each server are in the `local_data` subdirectory.

## Prerequisites:
- docker CE installed.
- docker-compose installed.
- Java 1.8 JDK and Maven installed and in PATH (Open JDK is fine)
- gcloud SDK installed and logged in with an account that has permissions to fetch images from our Google Cloud Repository (Entity Recognizer).
- gcloud set up as a Docker Credential helper (as per https://cloud.google.com/container-registry/docs/advanced-authentication#gcloud_as_a_docker_credential_helper)


## To build this
1. `mvn install` in the `service` directory of this Git repo.
1. `docker-compose build`

## To run this
1. `docker-compose up -d` to start downloading the other images and start the system.
1. `docker ps` to check all services are up as expected.

## To test this
The following ports are exposed outside of docker-compose:

| service   | port  |
|-----------|------:|
| API DB    | 10001 |
| API SVC   | 10002 |
| API CTRL  | 10003 |
| DUMMY EMB | 10004 |

## To debug the Java
The docker-compose setup exposes Java debugger on known ports so you can remote connect IntelliJ's remote debugger (or another standard Java debugger).

| debug of  | port  |
|-----------|------:|
| API SVC   | 11001 |
| API CTRL  | 11002 |
