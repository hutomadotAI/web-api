# Introduction 
Web-api is main Java API

# Local cloudbuild
You can use GCP cloud SDK to run cloud build.

```
gcloud builds submit --project opensource-239410 --substitutions=REPO_NAME=web-api,COMMIT_SHA=test,BRANCH_NAME=master
```
