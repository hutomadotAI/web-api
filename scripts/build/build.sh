#!/bin/bash
# A script to build a Python package, requiring venv to install some build tools

on_error() {
    echo "Error at $(caller), aborting"
    exit 1
}
trap on_error ERR

SCRIPT_DIR=`dirname $BASH_SOURCE`
echo "*** Setting up venv ***"
source "${SCRIPT_DIR}/setup_python.sh" || exit $?

echo "*** Doing build ***"
python "${SCRIPT_DIR}/build.py" $*
