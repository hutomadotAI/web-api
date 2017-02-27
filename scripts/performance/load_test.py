import random
import threading
import time
from threading import Lock, Thread

import hu_api.api
from performance.common import get_ai_name, read_training_file, lines_to_words, de_rate_limit


class AverageTime:
    def __init__(self):
        self.lock = Lock()
        self.total = 0.0
        self.count = 0

    def add(self, reading):
        with self.lock:
            self.total += reading
            self.count += 1

    def average(self):
        with self.lock:
            if self.count == 0:
                return 0.0
            return self.total / self.count


def load_test(config, requester, requesterLoad):

    def multipleRun(config, aiid, questionWords, answerWords, maxSimultaneous, totalRequests):
        multiAverage = AverageTime()
        semaphore = threading.BoundedSemaphore(maxSimultaneous)
        workers = [Thread(target=make_request, args=[config, aiid, questionWords, answerWords, multiAverage, semaphore]) for
                   p in
                   range(totalRequests)]
        for worker in workers:
            worker.start()
        for worker in workers:
            worker.join()
        return multiAverage


    def make_request(config, aiid, questionWords, answerWords, average, semaphore):
        howManyWordsInAQuestion = random.randrange(1, 4)
        question = ' '.join(random.sample(questionWords, howManyWordsInAQuestion))
        min_p = 0.0

        with semaphore:
            startTime = time.clock()
            chatResult = hu_api.api.load_test_chat(requesterLoad, aiid, question, history="", chat_id="", min_p=min_p)
            duration = time.clock() - startTime

        if not chatResult.success:
            print(chatResult.text)
        else:
            average.add(duration)

            answer = chatResult.response["result"]["answer"]
            answerChunks = [word for word in answer.split(' ') if word not in config.common_words]
            wrongWords = [word for word in answerChunks if word not in answerWords]
            if len(wrongWords):
                flag = 'BAD'
            else:
                flag = 'ok'

            print("{0:.2f} {1:.2f}".format(average.average(), duration), flag, '[', question, ']', answer)


    def multi_run(config, aiid, questionwords, answerwords, simultaneous, total, size, results):
        average = multipleRun(config, aiid, questionwords, answerwords, simultaneous, total).average()
        print('Single Size:{0} Average:{1:.2f}'.format(size, average))
        results.append((simultaneous, size, average))

    def find_in(name):
        if find.success:
            if "ai_list" in find.response:
                for ai in find.response["ai_list"]:
                    if name == ai["name"]:
                        return (ai["aiid"], ai)
        return ("", find.text)

    ai_targets = {}
    question_words = {}
    answer_words = {}
    common_words = set(config.common_words)

    find = de_rate_limit(hu_api.api.get_ai, requester)

    for size in config.training_sizes:
        name = get_ai_name(size)
        aiid, ai = find_in(name)
        if (aiid == ""):
            print(ai)
            print("{0} not found".format(name))
            exit(-1)
        ai_targets[size] = aiid

        training_filename = config.training_filename + "_" + str(size)
        questions, answers = read_training_file(training_filename)
        question_words[size] = lines_to_words(questions) - common_words
        answer_words[size] = lines_to_words(answers) - common_words

        log_line = "Found AI {0} with ID {1} status {2}, unique Q words {3}, A words {4}"
        print(log_line.format(name, aiid, ai["ai_status"], str(len(question_words[size])),
                              str(len(answer_words[size]))))

    results = []
    for size, aiid in ai_targets.items():
        for simultaneous, total in config.request_pattern:
            multi_run(config, aiid, question_words[size], answer_words[size], simultaneous, total, size, results)

    return results
