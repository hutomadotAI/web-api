import argparse
import requests
import time
import urllib
import os
import urllib3

import mysql.connector


def get_bots(cursor):
    print("Getting all bots and dev-tokens")
    bot_list = None
    bots_sql = "SELECT ai.aiid, users.dev_token FROM ai " \
               "INNER JOIN users ON ai.dev_id=users.dev_id AND ai.deleted='0'"
    cursor.execute(bots_sql)
    bot_list = cursor.fetchall()
    return bot_list


def get_published_bots(cursor):
    print("Getting published bots")
    bot_list = None
    bots_sql = "SELECT ai.aiid,  botStore.publishing_state FROM ai " \
               "INNER JOIN botStore ON ai.aiid=botStore.aiid"
    cursor.execute(bots_sql)
    bot_list = cursor.fetchall()

    bots = {}
    for aiid, publishing_state in bot_list:
        bots[aiid] = publishing_state
    return bots


def set_publishing_status(cnx, cursor, aiid, value):
    sql = "UPDATE `hutoma`.`botStore` " \
          "SET `publishing_state` ='{}' " \
          "WHERE `aiid` = '{}';".format(value, aiid)

    cursor.execute(sql)
    cnx.commit()


def retrain_bot(cnx, cursor, api_endpoint, aiid, dev_token, published_bots):
    print("- Processing bot {} -".format(aiid))
    headers = {'Authorization': "Bearer {}".format(dev_token)}

    training_url = "{}/ai/{}/training/update".format(api_endpoint, aiid)

    publishing_status = 0
    if aiid in published_bots:
        publishing_status = published_bots[aiid]

    if publishing_status > 0:
        print("Unsetting published status")
        set_publishing_status(cnx, cursor, aiid, 0)

    success = False
    retries = 3

    try:
        while not success and retries > 0:
            r = requests.put(training_url, headers=headers, verify=False)

            if r.status_code == 200:
                success = True
                print("success")
            elif r.status_code == 400 or r.status_code == 404:
                # Nothing we can do about this, bot doesn't exist or there's
                # something we don't know about.
                print(r.status_code, ":", r.text)
                success = True
            elif r.status_code == 429:
                # Rate limited, wait a minute and try again.
                print("rate limited, waiting...")
                time.sleep(2)
            else:
                # Generic catch all. Print and retry.
                print(r.status_code, ":", r.text)
                retries -= 1

        if not success:
            print("Failed bot: {}".format(aiid))
    finally:
        if publishing_status > 0:
            print("Resetting published status to {}".format(publishing_status))
            set_publishing_status(cnx, cursor, aiid, publishing_status)


def arg_error(parser, error_text):
    print("\n**** ERROR: {}! ****\n".format(error_text))
    parser.print_help()
    exit(-1)


def main(args, parser):
    """Main function"""
    # disable HTTPS warning
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    
    api_raw = args.api
    if api_raw is None or len(api_raw) == 0:
        api_raw = os.environ.get('HUTOMA_API_CLI_URL')

    if api_raw is None or len(api_raw) == 0:
        arg_error(parser, "URL is not set")

    url_parsed = urllib.parse.urlparse(api_raw)
    if (url_parsed.scheme != 'http' and url_parsed.scheme != 'https'):
        url_parsed = urllib.parse.urlparse('http://' + api_raw)
    url = url_parsed.geturl()
    print("API URL is", url)
    print("DB at {}:{}".format(args.dbhost, args.dbport))

    config = {
        'user': 'root',
        'password': args.dbpassword,
        'host': args.dbhost,
        'port': args.dbport,
        'database': 'hutoma',
        'raise_on_warnings': False,
        'buffered': True
    }

    cnx = mysql.connector.connect(**config)
    cursor = cnx.cursor()
    print("Connected to DB")
    try:
        bots = get_bots(cursor)
        published_bots = get_published_bots(cursor)
        for ai, dev_token in bots:
            retrain_bot(cnx, cursor, url, ai, dev_token, published_bots)
    finally:
        cursor.close()
        cnx.close()


if __name__ == "__main__":
    PARSER = argparse.ArgumentParser(
        description='Retrain all bots command-line')
    PARSER.add_argument(
        '--dbhost', help='MySql host machine', default="localhost")
    PARSER.add_argument('--dbport', help='MySql port', default=3306, type=int)
    PARSER.add_argument(
        '--dbpassword', help='MySql root password', default="password")
    PARSER.add_argument(
        '--api',
        help='API endpoint, e.g. https://api.hutoma.ai/v1, can be in ' +
        'HUTOMA_API_CLI_URL variable')
    ARGS = PARSER.parse_args()
    main(ARGS, PARSER)
