# Introduction 
The Hu:toma API lets you access and control your bots programmatically. The online demo only exposes a subset of those API but if you are standing up your own server you will be able to access to a variety of services:

1. AI Management 
2. Intent
3. Entities
4. Training
5. Chat
6. Analytics
7. Integrations
8. Console Admin

The API uses Json Web Tokens (JWT) to securely authenticate and authorize each request while maintaining low overhead and the ability to be used across different domains. Each developer is assigned a pair of JWT tokens, which also include in their payload the plan the developer is subscribed to. This means that if you switch plans at some stage, the tokens will change.

# API Documentation
API documentation can be found at https://help.hutoma.ai/

# Local cloudbuild
You can use GCP cloud SDK to run cloud build.

```
gcloud builds submit --project opensource-239410 --substitutions=REPO_NAME=web-api,COMMIT_SHA=test,BRANCH_NAME=master
```

# Questions or Help
Please visit our community at https://community.hutoma.ai
