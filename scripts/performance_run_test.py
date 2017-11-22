import argparse
import os
import urllib
from threading import Thread

import hu_api
from performance.common import check_api_available, de_rate_limit
from performance.performance_config import Config, make_config


def get_ai_information(aiid, requester, found_ais):

    bot = de_rate_limit(hu_api.api.get_ai, requester, aiid)
    print('Found AI {} at {}'.format(bot.response['name'], aiid))
    found_ais[bot.response['name']] = aiid


def chat_ai(aiid, name, requester):

    chat = de_rate_limit(hu_api.api.chat, requester, aiid, "I'm blurred")
    if chat and chat.response and chat.response['status'] and chat.response['status']['code']:
        code = chat.response['status']['code']
        if code != 200:
            print("{} ERR {}".format(name, code.str()))
        else:
            print("{} OK {}".format(name, chat.response['result']['answer']))
    else:
        print("{} ERROR {}".format(name, chat.status_code.str()))


def main():

    def find_ais(ai_defs):

        found_ais = {}
        threads = []

        for aiid in hu_api.api.find_ais(requester, ai_defs.ai_prefix):
            thread = Thread(target=get_ai_information, args=(aiid, requester, found_ais))
            thread.start()
            threads.append(thread)

        for thread in threads:
            thread.join()

        return found_ais


    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
    requester_load = hu_api.api.ApiRequester(config.url_root, config.chat_auth, [])

    check_api_available(requester)

    chat_test_defs = config.chat_test_ais

    botlist = find_ais(chat_test_defs)

    threads = []
    for name, aiid in botlist.items():
        thread = Thread(target=chat_ai, args=(aiid, name, requester))
        threads.append(thread)
        thread.start()

    for thread in threads:
        thread.join()


if __name__ == "__main__":
    config = make_config()
    main()
