# Introduction 
Web-api consist of two part Java API and PHP web UI

# Build, run and test

## Web

```bash
cd web/src
```

## Build

Build container and initials statics

```bash
docker-compose build local-console
docker-compose run --rm local-console make build-manifest
```

## Watch

Needs to have `fswatch` installed. Automatically build JS while coding.

```bash
make watch
```
