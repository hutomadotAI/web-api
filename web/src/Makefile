SHELL   := /bin/bash

UGLIFYJS  := ./node_modules/.bin/uglifyjs

UGLIFYJS_FLAGS := --compress

# Manifest file where we store mapping for build assets
MANIFEST_FILE := console/dist/manifest.php

JS_DIRECTORY := console/scripts
ASSETS_DIRECTORY := console/dist/assets
JS_ASSETS := $(subst $(JS_DIRECTORY)/,$(ASSETS_DIRECTORY)/,$(wildcard $(JS_DIRECTORY)/**/*.js))

$(ASSETS_DIRECTORY)/%.js: $(JS_DIRECTORY)/%.js
	@mkdir -p $(@D)
	@cp $< $@

all: build-assets

clean-manifest:
	@echo "> clean-manifest"
	@rm -f $(MANIFEST_FILE)

clean-assets: clean-manifest
	@echo "> clean-assets"
	@rm -rf $(ASSETS_DIRECTORY)/*

clean: clean-assets
	@echo "> clean"

watch: clean-manifest
	fswatch -o $(JS_DIRECTORY) | xargs -n1 -I{} $(MAKE) build-manifest

build-assets: clean-assets $(JS_ASSETS)

compress-assets: build-assets
	@echo "> compress-assets"
	@for filename in $(subst $(JS_DIRECTORY)/,$(ASSETS_DIRECTORY)/,$(wildcard $(JS_DIRECTORY)/**/*.js)); do \
		echo $$filename; \
		$(UGLIFYJS) $$filename $(UGLIFYJS_FLAGS) --output $$filename; \
	done

build-hash: compress-assets
	# For all the build files create generate hashes, rename files and create an
	# entry in the manifest file
	@echo "> build-hash"
	# 1. Get all output files
	# 2. Create a MD5 hash from the content;
	# 3. Substring hash to a shorten version;
	# 4. Add hash to filename;
	# 5. Replace original file;
	# 6. Add file mapping to manifest;
	@echo "<?php \$$manifest = [" >> $(MANIFEST_FILE)
	@for filename in $(subst $(JS_DIRECTORY)/,$(ASSETS_DIRECTORY)/,$(wildcard $(JS_DIRECTORY)/**/*.js)); do \
		hash=($$(md5sum $$filename)); \
		hash=$${hash:0:8}; \
		hashed_filename="$${filename%%.*}-$$hash.$${filename#*.}"; \
		mv $$filename $$hashed_filename; \
		echo "\"$${filename#$(ASSETS_DIRECTORY)/}\" => \"$$hashed_filename\"," >> $(MANIFEST_FILE); \
	done
	@echo "]; ?>" >> $(MANIFEST_FILE)

build-manifest: build-assets
	# For all the build files create generate hashes, rename files and create an
	# entry in the manifest file
	@echo "> build-manifest"
	# 1. Get all output files
	# 6. Add file mapping to manifest;
	@echo "<?php \$$manifest = [" >> $(MANIFEST_FILE)
	@for filename in $(subst $(JS_DIRECTORY)/,$(ASSETS_DIRECTORY)/,$(wildcard $(JS_DIRECTORY)/**/*.js)); do \
		echo "\"$${filename#$(ASSETS_DIRECTORY)/}\" => \"$$filename\"," >> $(MANIFEST_FILE); \
	done
	@echo "]; ?>" >> $(MANIFEST_FILE)

build: build-hash
