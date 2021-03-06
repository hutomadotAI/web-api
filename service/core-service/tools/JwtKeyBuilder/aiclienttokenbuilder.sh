#!/bin/bash
#
# How to run this script:
#  ./aiclienttokenbuilder.sh "-devid 00000000-0000-0000-0000-000000000000 -enc AAAAAAAAAAAAAA= -aiid 00000000-0000-0000-0000-000000000000"
#
# where devid is the UUID of the developer, enc is the encoding key (secret, in base64), and aiid is the AI id

mvn compile && mvn exec:java@AiClientTokenBuilder -Dexec.executable="java" -Dexec.args="$@"