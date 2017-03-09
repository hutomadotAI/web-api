import time

from requests.packages.urllib3.exceptions import NewConnectionError

import hu_api


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


