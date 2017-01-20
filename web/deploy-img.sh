#!/bin/bash
tag=$1

echo '******* Building Dev-Console image with tag '"$tag"'...'
gcloud docker -- rmi eu.gcr.io/hutoma-backend/dev-console:$tag
gcloud docker -- build -t eu.gcr.io/hutoma-backend/dev-console:$tag .
echo '******* Pushing image to repository...'
gcloud docker -- push eu.gcr.io/hutoma-backend/dev-console:$tag
echo '******* Done'