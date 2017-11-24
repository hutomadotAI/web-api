
import random
from threading import Thread

import time

from pathlib import Path

import hu_api
from performance.common import check_api_available, de_rate_limit, read_training_file, lines_to_words, find_ais, chat_ai
from performance.performance_config import Config, make_config


def main():

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    chat_test_defs = config.chat_test_ais

    botlist = find_ais(requester, chat_test_defs)

    requests_per_bot = 100
    max_simultaneous = 100

    non_words = {'xyzzy', 'qwklpqlwkqq', 'mnbdqwtplqww'}
    aiml_bot_id = '40bf861c-7289-4c9b-960c-5b917879cb43'
    server = config.server

    while True:
        started_at = time.time()

        # ten seconds per round
        next_one_after = started_at + 10

        threads = []
        for name in sorted(botlist.keys()):
            (aiid, words) = botlist[name]

            if server == 'rnn':
                words = non_words
            if server == 'aiml':
                aiid = aiml_bot_id
                requests_per_bot = max_simultaneous

            for request_id in range(0, requests_per_bot):

                # only the first 10 bots so as not to thrash bot-data
                if len(threads) < max_simultaneous:
                    threaded_requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
                    thread = Thread(target=chat_ai, args=(aiid, name, words, threaded_requester))
                    threads.append(thread)
                    thread.start()

        for thread_join in threads:
            thread_join.join()

        time_now = time.time()
        time_to_wait = next_one_after - time_now

        if time_to_wait > 0:
            print('Waiting {} seconds for the next round ...'.format(time_to_wait))
            time.sleep(time_to_wait)
        else:
            print('Round is {} seconds late ..... starting now'.format(0 - time_to_wait))


if __name__ == "__main__":
    config = make_config()
    main()
