#!/bin/bash
tag=$1

echo '******* Building API-DB image with tag '"$tag"'...'
gcloud docker -- rmi eu.gcr.io/hutoma-backend/api-db:$tag
gcloud docker -- build -t eu.gcr.io/hutoma-backend/api-db:$tag .
echo '******* Pushing image to repository...'
gcloud docker -- push eu.gcr.io/hutoma-backend/api-db:$tag
echo '******* Done'