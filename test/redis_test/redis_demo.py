#!/usr/bin/env python
"""Script to build code"""
import argparse
import datetime
import os
import re
from pathlib import Path
import subprocess
import redis


def main(args):
    """Main function"""
    client = redis.StrictRedis(host=args.host,
        port=args.port, charset="utf-8", decode_responses=True)

    ttl = client.ttl(args.chat_id)
    print ("----")
    print("Chat ID {} has time-to-live of {}s".format(args.chat_id, ttl))

    all_data = client.hgetall(args.chat_id)
    print("DEV {}, AI {}".format(all_data["devid"],all_data["aiid"]))
    print ("----")
    print(all_data["state"])
    print ("----")

if __name__ == "__main__":
    PARSER = argparse.ArgumentParser(
        description='Redis chat-state demo command-line')
    PARSER.add_argument(
        'chat_id', help='Chat id')
    PARSER.add_argument(
        '--host', help='Redis machine', default="10.180.0.4")
    PARSER.add_argument(
        '--port', help='Redis port', default=30379, type=int)
    ARGS = PARSER.parse_args()
    main(ARGS)
