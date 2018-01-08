
import random
from math import copysign
from queue import Queue
from threading import Thread

import time

from pathlib import Path

import hu_api
from performance.common import check_api_available, de_rate_limit, read_training_file, lines_to_words, find_ais, \
    chat_ai, write_file_lines
from performance.performance_config import Config, make_config

TARGET_LATENCY_SECONDS = 2.5
TIME_WINDOW_SECONDS = 10.0
RUN_LENGTH_SECONDS = 5.0 * 60.0
LOAD_MIN = 10.0
LOAD_MAX = 100.0
SPEED_SCALING_FACTOR = 2.0

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

        chat_id = {}
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

                chat = hu_api.api.load_test_chat(requester, aiid, question, chat_id.get(name, ''))
                # clear chat_id here so that we start with a new chat if there is an error
                chat_id[name] = ''
                if chat and chat.response and chat.response['status'] and chat.response['status']['code']:
                    code = chat.response['status']['code']
                    if code != 200:
                        result = "{} {} ERR Q\"{}\" {}".format(self.index, name, question, code)
                    else:
                        result = "{} {} OK Q\"{}\" A\"{}\""\
                            .format(self.index, name, question, chat.response['result']['answer'])
                        # only keep the chat_id if the chat was successful
                        chat_id[name] = chat.response['chatId']
                        success = True
                else:
                    result = "{} {} ERROR {}"\
                        .format(self.index, name, chat.status_code)
            except Exception as e:
                result = str(e)
            finally:
                end_time = time.time()
                duration = end_time - self.start_time
                self.results.put((success, self.start_time, end_time, duration, result))

            self.start_time = 0.0
            if duration < 1.0:
                sleep_time = (1.0 - duration) + (0.1 * (random.random() - 0.5))
                if sleep_time > 0.0:
                    time.sleep(sleep_time)

    def stop(self):
        self.execute = False

    def get_running_duration(self):
        return (time.time() - self.start_time) if self.start_time > 1.0 else 0.0


def create_non_words():
    words = set()
    alphabet = list('nsrhldcumfpgwybvkxjqz') # last bit of etaoinsrhldcumfpgwybvkxjqz
    while len(words) < 10:
        char_list = []
        while len(char_list) < 10:
            char_list.append(random.sample(alphabet, 1)[0])
        words.add(''.join(char_list))
    return words


def main():

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    chat_test_defs = config.chat_test_ais

    bot_list = find_ais(requester, chat_test_defs, set(config.common_words), config.aiml_words)

    if not bot_list:
        print("No data")
        exit(0)

    server = config.server

    if server == 'rnn':
        bot_list = {name: (aiid, create_non_words()) for (name, (aiid, words)) in bot_list.items()}
    elif server == 'aiml':
        bot_list = {name: value for (name, value) in bot_list.items() if name.find('AIML') > -1}
    elif server == 'intent':
        bot_list = {name: value for (name, value) in bot_list.items() if name.find('Intent') > -1}
    else:
        bot_list = {name: value for (name, value) in bot_list.items() if
                    name.find('AIML') == -1 and name.find('Intent') == -1}

    # target only a single bot
    use_bots = set(sorted(bot_list.keys())[:config.bots])
    bot_list = {name: v for (name, v) in bot_list.items() if name in use_bots}

    threads = []
    load = LOAD_MIN
    result_queue = Queue()

    all_results = []
    result_window = []

    run_starts_at = time.time()
    run_ends_at = run_starts_at + RUN_LENGTH_SECONDS

    while time.time() < run_ends_at:

        need_active = max(1, int(round(load)))
        while need_active > len(threads):

            threaded_requester = hu_api.api.ApiRequester(config.url_root, config.chat_auth, [])
            thread = Worker(args=(bot_list, threaded_requester), results=result_queue, index=len(threads))
            threads.append(thread)
            thread.start()

        while need_active < len(threads):
            threads.pop().stop()

        while not result_queue.empty():
            (success, start_time, end_time, duration, result) = result_queue.get(False)
            all_results.append((success, start_time - run_starts_at, end_time - run_starts_at, duration, result))
            result_window.append((success, end_time, duration))
            success_message = "OK" if success else "ERROR"
            print("    {} {} {}".format(success_message, str(round(duration, 2)), result))
            changes = True

        #take an average latency for anything that completed in the last n seconds
        valid_in_window = time.time() - TIME_WINDOW_SECONDS
        result_window = [x for x in result_window if x[1] > valid_in_window]

        #calculate weighted average
        total_weight = 0.0
        latency_sum = 0.0

        for (success, end_time, duration) in result_window:
            weight = ((end_time - valid_in_window) / TIME_WINDOW_SECONDS) ** 3
            total_weight += weight
            latency_sum += (weight * duration) if success else weight * (TARGET_LATENCY_SECONDS * 6.0)
            #print("        FIN {} time {} factor {}".format("okk" if success else "ERR", duration, weight))

        # weighted average for everything that terminated
        average_access_time_complete = latency_sum / total_weight \
            if total_weight > 0.1 else TARGET_LATENCY_SECONDS

        # get a list of everything in progress that is taking longer than average
        in_progress_times = [thread.get_running_duration() for thread in threads]
        running_times = [x for x in in_progress_times if x > 1.0 and x > average_access_time_complete]

        # debug
        for duration in running_times:
            print("        RUN {} time {}".format("___", duration))
        print("\n")

        # factor running timings into the average
        total_weight += len(running_times)
        latency_sum += sum(running_times)

        # recalculate an average out of complete and running
        average_access_time = latency_sum / total_weight \
            if total_weight > 0.1 else TARGET_LATENCY_SECONDS

        # count successes an failures
        window_error_count = len([x for x in result_window if not x[0]])
        window_total_count = len(result_window)

        # calculate the new P term
        diff = TARGET_LATENCY_SECONDS - average_access_time
        load_correction = copysign(((abs(diff) / TARGET_LATENCY_SECONDS) ** 1.2) * 0.2 * SPEED_SCALING_FACTOR, diff)

        print("Simultaneous {}({}) lat_metric {} complete_lat {} results {} errors {} diff {} run {}%".format(
            len(threads), round(load, 3),
            round(average_access_time, 3),
            round(average_access_time_complete, 3),
            window_total_count, window_error_count, round(load_correction, 3),
            int(100.0 - 100.0 * ((run_ends_at - time.time()) / RUN_LENGTH_SECONDS))
        ))
        load = min(LOAD_MAX, max(LOAD_MIN, load + load_correction))

        time.sleep(1.0)

    while len(threads):
        threads.pop().stop()

    csv_output = ["\n\ntime,success_count,fail_count,average_latency"]
    for time_point in range(0, int(RUN_LENGTH_SECONDS)):

        running = [duration for (success, start_time, end_time, duration, result) in all_results \
                   if (start_time < float(time_point) <= end_time) or int(round(end_time)) == time_point]
        latency = sum(running) / len(running) if running else 0.0

        ended = [success for (success, start_time, end_time, duration, result) in all_results \
                 if int(round(end_time)) == time_point]
        successes = len([success for success in ended if success])
        failures = len([success for success in ended if not success])

        csv_output.append("{},{},{},{}".format(time_point, successes, failures, latency))

    write_file_lines("results.csv", csv_output)


if __name__ == "__main__":
    config = make_config()
    main()
