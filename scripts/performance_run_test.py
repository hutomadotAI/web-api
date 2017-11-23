import argparse
import os
import random
import urllib
from threading import Thread

import time

from pathlib import Path

import hu_api
from performance.common import check_api_available, de_rate_limit, read_training_file, lines_to_words
from performance.performance_config import Config, make_config


def get_ai_information(aiid, requester, found_ais, ai_defs):

    def load_training(filename):

        if not Path(filename).is_file():
            print("Missing {0}".format(filename))
            words = {'missing'}
        else:
            (questions, answers) = read_training_file(filename)
            words = lines_to_words(questions)

        return words

    bot = de_rate_limit(hu_api.api.get_ai, requester, aiid)
    print('Found AI {} at {}'.format(bot.response['name'], aiid))
    filename = (bot.response['name']
          .replace(ai_defs.ai_prefix, ai_defs.file_prefix)
          .replace('-', ''))
    found_ais[bot.response['name']] = (aiid, load_training(filename))


def chat_ai(aiid, name, words, requester):

    question = '{} {}'.format(
        random.sample(words, 1)[0],
        random.sample(words, 1)[0])

    chat = de_rate_limit(hu_api.api.chat, requester, aiid, question)
    if chat and chat.response and chat.response['status'] and chat.response['status']['code']:
        code = chat.response['status']['code']
        if code != 200:
            print("{} ERR {}".format(name, code))
        else:
            print("{} OK Q\"{}\" A\"{}\"".format(name, question, chat.response['result']['answer']))
    else:
        print("{} ERROR {}".format(name, chat.status_code))


def main():

    def find_ais(ai_defs):

        found_ais = {}
        threads = []

        for aiid in hu_api.api.find_ais(requester, ai_defs.ai_prefix):
            thread = Thread(target=get_ai_information, args=(aiid, requester, found_ais, ai_defs))
            thread.start()
            threads.append(thread)

        for thread in threads:
            thread.join()

        return found_ais

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    chat_test_defs = config.chat_test_ais

    botlist = find_ais(chat_test_defs)

    while True:
        started_at = time.time()
        next_one_after = started_at + 10

        threads = []
        for name, (aiid, words) in botlist.items():
            threaded_requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
            thread = Thread(target=chat_ai, args=(aiid, name, words, threaded_requester))
            threads.append(thread)
            thread.start()

        for thread_join in threads:
            thread_join.join()

        time_now = time.time()
        time_to_wait = next_one_after - time_now
        break
        if time_to_wait > 0:
            print('Waiting {} seconds for the next round ...'.format(time_to_wait))
            time.sleep(time_to_wait)
        else:
            print('Round is {} seconds late ..... starting now'.format(0 - time_to_wait))


if __name__ == "__main__":
    config = make_config()
    main()
