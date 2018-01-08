import random
import time
from threading import Thread

from pathlib import Path
from requests.packages.urllib3.exceptions import NewConnectionError

import hu_api
from performance.performance_config import Config


def write_file_lines(fileName, lineList):
    with open(fileName, "w") as file:
        file.writelines([word + '\n' for word in lineList])


def read_lines(fileName):
    with open(fileName, "r") as file:
        return file.readlines()


def read_words_from_file(fileName):
    with open(fileName, "r") as file:
        lines = [line.strip() for line in read_lines(fileName)]
        return [word for word in lines if word != ""]


def read_training_file(filename):
    lines = read_lines(filename)
    questions = []
    answers = []

    question_is_next = True
    for line in [line.strip() for line in lines]:
        if not line:
            question_is_next = True
        elif question_is_next:
            questions.append(line)
            question_is_next = False
        else:
            answers.append(line)
            question_is_next = True

    return (questions, answers)


def lines_to_words(lines):
    words = set()
    for line in lines:
        words |= set(line.split())
    return words

def de_rate_limit(api_call, *args):
    while True:
        response = api_call(*args)
        if response.status_code != 429:
            break
        time.sleep(0.25)
    return response

def check_api_available(requester):
    api_found = False
    try:
        status_request = de_rate_limit(hu_api.api.get_ai, requester, '00000000-0000-0000-0000-000000000000')
    except:
        print("Could not contact any service at given URL")
        exit(0)

    if status_request.status_code == 404:
        try:
            message = status_request.response
            api_found = True
        except AttributeError:
            api_found = False
    if not api_found:
        print("Cannot contact API at given url. Failed with error {0}".format(str(status_request.status_code)))
        exit(0)

def get_ai_information(aiid, requester, found_ais, ai_defs, common_words, aiml_words):

    def load_training(filename):

        if not Path(filename).is_file():
            print("Missing {0}".format(filename))
            words = {'missing'}
        else:
            (questions, answers) = read_training_file(filename)
            words = lines_to_words(questions)

        words -= common_words
        return words

    bot = de_rate_limit(hu_api.api.get_ai, requester, aiid)
    bot_name = bot.response['name']
    print('Found AI {} at {} with state {}'.format(bot_name, aiid, bot.response['training']))
    if bot_name.find('AIML') > -1:
        found_ais[bot_name] = (aiid, aiml_words)
    elif bot_name.find('intent') > -1:
        intent_words = {'red', 'blue', 'green', 'nothing'}
        intent_words.update(set(["intent_trigger_{}".format(x) for x in range(10)]))
        found_ais[bot_name] = (aiid, intent_words)
    else:
        filename = (bot_name
                    .replace(ai_defs.ai_prefix, ai_defs.file_prefix)
                    .replace('-', ''))
        found_ais[bot_name] = (aiid, load_training(filename))


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


def find_ais(requester, ai_defs, common_words, aiml_words):

    found_ais = {}
    threads = []

    for aiid in hu_api.api.find_ais(requester, ai_defs.ai_prefix):
        thread = Thread(target=get_ai_information, args=(aiid, requester, found_ais, ai_defs, common_words, aiml_words))
        thread.start()
        threads.append(thread)

    for thread in threads:
        thread.join()

    return found_ais
