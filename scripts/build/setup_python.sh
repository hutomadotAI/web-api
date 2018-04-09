#!/bin/bash
# A script to setup a Python 3.5 virtual environment
# So that project Python setup doesn't mess up main machine
# or other projects.

on_error() {
    echo "Error at $(caller), aborting"
    # don't exit, the trap will break, but set the return code
    RETURN=1
}

# script will always be sourced - so break the loop on error
# set RETURN value
RETURN=0
while true; do
  trap 'on_error; break' ERR
  SCRIPT_DIR=`dirname $BASH_SOURCE`
  VE_DIR="${SCRIPT_DIR}/venv"
  if [ ! -d $VE_DIR ]; then
    echo Initializing virtualenv at $VE_DIR
    python3.5 -m venv $VE_DIR
  fi

  echo Entering Python 3.5 virtual environment at $VE_DIR
  source $VE_DIR/bin/activate
  pip install --upgrade pip

  echo "Installing build requirements"
  pip install --upgrade -r ${SCRIPT_DIR}/requirements.ini
  break;
done
trap - ERR
return $RETURN;