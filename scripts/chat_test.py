
import random
from queue import Queue
from threading import Thread

import time

from pathlib import Path

import hu_api
from performance.common import check_api_available, de_rate_limit, read_training_file, lines_to_words, find_ais, chat_ai
from performance.performance_config import Config, make_config


class Worker(Thread):
    """ Thread executing tasks from a given tasks queue """
    def __init__(self, args, results):
        Thread.__init__(self)
        self.args = args
        self.execute = True
        self.results = results

    def run(self):
        while self.execute:
            (botlist, requester) = self.args

            name = random.sample(botlist.keys(), 1)[0]
            (aiid, words) = botlist[name]

            start_time = time.time()
            success = False
            result = ''

            try:
                question = '{} {}'.format(
                    random.sample(words, 1)[0],
                    random.sample(words, 1)[0])

                chat = de_rate_limit(hu_api.api.chat, requester, aiid, question)
                if chat and chat.response and chat.response['status'] and chat.response['status']['code']:
                    code = chat.response['status']['code']
                    if code != 200:
                        result = "{} ERR {}".format(name, code)
                    else:
                        result = "{} OK Q\"{}\" A\"{}\"".format(name, question, chat.response['result']['answer'])
                        success = True
                else:
                    result = "{} ERROR {}".format(name, chat.status_code)
            except Exception as e:
                result = str(e)
            finally:
                end_time = time.time()
                duration = end_time - start_time
                self.results.put((success, end_time, duration, result))

    def stop(self):
        self.execute = False


def main():

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    chat_test_defs = config.chat_test_ais

    botlist = find_ais(requester, chat_test_defs)

    if not botlist:
        print("No data")
        exit(0)

    non_words = {'xyzzy', 'qwklpqlwkqq', 'mnbdqwtplqww'}
    aiml_bot_id = '40bf861c-7289-4c9b-960c-5b917879cb43'
    server = config.server

    if server == 'rnn':
        botlist = {name: (aiid, non_words) for (name, (aiid, words)) in botlist}
    if server == 'aiml':
        botlist = {'aiml': (aiml_bot_id, non_words) for (name, (aiid, words)) in botlist}

    threads = []
    load = 1.0
    result_queue = Queue()

    result_window = []

    while True:
        need_active = int(round(load))
        while need_active > len(threads):

            threaded_requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
            thread = Worker(args=(botlist, threaded_requester), results=result_queue)
            threads.append(thread)
            thread.start()

        while need_active < len(threads):
            threads.pop().stop()

        while not result_queue.empty():
            (success, end_time, duration, result) = result_queue.get(False)
            result_window.append((success, end_time, duration))
            success_message = "OK" if success else "ERROR"
            print("{} {} {}".format(success_message, str(round(duration, 2)), result))

        time.sleep(1.0)
        valid_in_window = time.time() - 10.0
        result_window = [x for x in result_window if x[1] > valid_in_window]

        average_access_time = sum([duration for (_, _, duration) in result_window]) / len(result_window) \
            if len(result_window) else 0.0
        print("Simult {} latency {}".format(load, average_access_time))


if __name__ == "__main__":
    config = make_config()
    main()
