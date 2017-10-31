#!/bin/bash
#
# How to run this script:
#  ./tokenbuilder.sh "-devid 00000000-0000-0000-0000-000000000000 -enc AAAAAAAAAAAAAA= -role ROLE_FREE"
#
# where devid is the UUID of the developer, enc is the encoding key (secret, in base64), and the role
# one of the acceptable roles

mvn compile && mvn exec:java@TokenBuilder -Dexec.executable="java" -Dexec.args="$@"