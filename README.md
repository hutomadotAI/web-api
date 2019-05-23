# Introduction 
The Hu:toma API lets you access and control your bots programmatically. The online demo only exposes a subset of those API but if you are standing up your own server you will be able to access to a variety of services:

1. AI Management 
2. Intent
3. Entities
4. Training
5. Chat
7. Integrations

The API uses Json Web Tokens (JWT) to securely authenticate and authorize each request while maintaining low overhead and the ability to be used across different domains. Each developer is assigned a pair of JWT tokens, which also include in their payload the plan the developer is subscribed to. This means that if you switch plans at some stage, the tokens will change.

# API Documentation
API documentation can be found at https://help.hutoma.ai/

# Build and run

For this you will need:
- Java JDK 8
- Maven
- Docker
- Docker-compose

The project contains 3 components:
1. Database
2. Controller
3. Core service
4. Fluent logger

First, you will need to compile the Java code. To do this, download the code and run:
```
cd {location_of_the_code}/service
mvn package
```
This will take a while to build, as it will need to download all the Maven packages, run the tests and generate the WAR files. When this is complete, the next step is to generate the Docker containers for the 3 components:
```
cd {code_location}/db
docker build -t hu_api_db .

cd {code_location}/service/controller-service
docker build -t hu_api-controller .

cd {code_location}/service/core-service
docker build -t hu_api-core .
```

## Running the services using `docker-compose`
_Note that this will sucessfully spin up the API related components, but you will not have a UI front-end or any conversational back-ends. To launch the full system, please refer to the [Docker-Containers repo](https://github.com/hutomadotAI/Docker-Containers), and replace the image names with the ones generated during the build step described above._

This is the easiest way to launch the API components:
```
cd {code_location}
docker-compose up
```

To stop the services, type:
```
docker-compose down
```

## Running the services using `docker-run`
*Please note that the `docker run` commands shown are for reference only. You should use docker-compose to simplify running the services together, or alternatively docker swarm, kubernetes, or another orchestrator.*

### Create the shared network for the containers:
```
docker network create hu_net
```


### Run the fluent logger:
```
cd {code_location}/fluentd
docker run \
    -p 24224:24224 \
    -v /tmp/fluentd:/fluentd/log -v $(pwd)/fluent.conf:/fluentd/etc/fluent.conf \
    --name fluentd \
    --network hu_net \
    fluent/fluentd:v1.1.1-onbuild
```


### Run the database:
```
docker run \
    -p 13306:3306 \
    -v /tmp/db_data:/var/lib/mysql \
    --name hu_api_db \
    --network hu_net \
    hu_api_db
```

### Run the API controller:
```
docker run -p 8081:8080 \
    -e "API_CONNECTION_STRING=jdbc:mysql://hu_api_db:3306/hutoma?user=hutoma_caller&password=%3EYR%22khuN*.gF)V4%23&zeroDateTimeBehavior=convertToNull" \
    -e "API_LOGGING_FLUENT_HOST=fluentd" -e "API_LOGGING_FLUENT_PORT=24224" \
    --name hu_api-ctrl \
    --network hu_net \
    hu_api-controller
```

### Run the API core:
```
docker run -p 8080:8080 \
    -e "API_ENCODING_KEY=L0562EMBfnLadKy57nxo9btyi3BEKm9m+DoNvGcfZa+DjHsXwTl+BwCE4NeKEAagfkhYBFvhvJoAgtugSsQOfw==" \
    -e "API_CONNECTION_STRING=jdbc:mysql://hu_api_db:3306/hutoma?user=hutoma_caller&password=%3EYR%22khuN*.gF)V4%23&zeroDateTimeBehavior=convertToNull" \
    -e "API_LOGGING_FLUENT_HOST=fluentd" \
    -e "API_LOGGING_FLUENT_PORT=24224" \
    -e "API_CONTROLLER_URL=http://hu_api-ctrl:8080/v1" \
    --name hu_api-core \
    --network hu_net \
    hu_api-core
```

# Local cloudbuild
You can use GCP cloud SDK to run cloud build.

```
gcloud builds submit --project opensource-239410 --substitutions=REPO_NAME=web-api,COMMIT_SHA=test,BRANCH_NAME=master
```

# Questions or Help
Please visit our community at https://community.hutoma.ai
