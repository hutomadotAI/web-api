import time


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


def get_ai_name(size):
    return "Load-Test-" + str(size)


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