#!/bin/bash
# A script to build a Python package, requiring venv to install some build tools

on_error() {
    echo "Error at $(caller), aborting"
    exit 1
}
trap on_error ERR


SCRIPT_DIR=`dirname $BASH_SOURCE`

pushd $SCRIPT_DIR
python3 -m pipenv sync --dev
python3 -m pipenv run python build.py $*
