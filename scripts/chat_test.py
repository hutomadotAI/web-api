
import random
from math import copysign
from queue import Queue
from threading import Thread

import time

from pathlib import Path

import hu_api
from performance.common import check_api_available, de_rate_limit, read_training_file, lines_to_words, find_ais, chat_ai
from performance.performance_config import Config, make_config

TARGET_LATENCY_SECONDS = 10.0
TIME_WINDOW_SECONDS = 10.0

class Worker(Thread):
    """ Thread executing tasks from a given tasks queue """
    def __init__(self, args, results, index):
        Thread.__init__(self)
        self.args = args
        self.execute = True
        self.results = results
        self.start_time = 0.0
        self.index = index

    def run(self):
        while self.execute:
            (botlist, requester) = self.args

            name = random.sample(botlist.keys(), 1)[0]
            (aiid, words) = botlist[name]

            self.start_time = time.time()
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
                        result = "{} {} ERR {}".format(self.index, name, code)
                    else:
                        result = "{} {} OK Q\"{}\" A\"{}\""\
                            .format(self.index, name, question, chat.response['result']['answer'])
                        success = True
                else:
                    result = "{} {} ERROR {}"\
                        .format(self.index, name, chat.status_code)
            except Exception as e:
                result = str(e)
            finally:
                end_time = time.time()
                duration = end_time - self.start_time
                self.start_time = 0.0
                self.results.put((success, end_time, duration, result))

    def stop(self):
        self.execute = False

    def get_running_duration(self):
        return (time.time() - self.start_time) if self.start_time > 1.0 else 0.0

def main():

    # for average_access_time in range(0, 21):
    #     diff = 10.0 - average_access_time
    #     access_diff = copysign(((abs(diff) / 10.0) ** 1.2) * 1.2, average_access_time)
    #     print("{} {}".format(round(average_access_time, 3), access_diff))
    # exit(0)

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
        botlist = {name: (aiid, non_words) for (name, (aiid, words)) in botlist.items()}
    if server == 'aiml':
        botlist = {'aiml': (aiml_bot_id, non_words) for (name, (aiid, words)) in botlist.items()}


    # target only a single bot
    first_bot = sorted(botlist.keys())[0]
    botlist = {name: v for (name, v) in botlist.items() if name == first_bot}

    threads = []
    load = 1.0
    result_queue = Queue()

    result_window = []
    changes = False

    while True:
        time.sleep(1.0)

        need_active = max(1, int(round(load)))
        while need_active > len(threads):

            threaded_requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
            thread = Worker(args=(botlist, threaded_requester), results=result_queue, index=len(threads))
            threads.append(thread)
            thread.start()

        while need_active < len(threads):
            threads.pop().stop()

        while not result_queue.empty():
            (success, end_time, duration, result) = result_queue.get(False)
            result_window.append((success, end_time, duration))
            success_message = "OK" if success else "ERROR"
            print("    {} {} {}".format(success_message, str(round(duration, 2)), result))
            changes = True

        valid_in_window = time.time() - TIME_WINDOW_SECONDS
        result_window = [x for x in result_window if x[1] > valid_in_window]

        window_times = [duration for (success, _, duration) in result_window if success] + \
            [(TARGET_LATENCY_SECONDS * 2.0) for (success, _, duration) in result_window if not success]

        average_access_time_complete = sum(window_times) / len(window_times) \
            if len(result_window) else TARGET_LATENCY_SECONDS

        in_progress_times = [thread.get_running_duration() for thread in threads]
        running_times = [x for x in in_progress_times if x > 1.0 and x > average_access_time_complete]

        average_access_time = (sum(window_times) + sum(running_times)) / \
                              (len(window_times) + len(running_times))\
            if len(result_window) else TARGET_LATENCY_SECONDS

        window_error_count = len([x for x in result_window if not x[0]])
        window_total_count = len(result_window)

        diff = TARGET_LATENCY_SECONDS - average_access_time
        load_correction = copysign(((abs(diff) / 10.0) ** 1.2) * 0.5, diff)

        if changes:
            print("Simultaneous {}({}) lat_metric {} complete_lat {} results {} errors {} diff {}".format(
                len(threads), round(load, 3),
                round(average_access_time, 3),
                round(average_access_time_complete, 3),
                window_total_count, window_error_count, round(load_correction, 3)))
            load = max(1.0, load + load_correction)
            changes = False


if __name__ == "__main__":
    config = make_config()
    main()
