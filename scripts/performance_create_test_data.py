import random
from pathlib import Path

import time

import hu_api.api
from performance.common import read_lines, write_file_lines, de_rate_limit, check_api_available
from performance.performance_config import Config, make_config

def main():

    def delete_test_ais(ai_defs):
        for aiid in hu_api.api.find_ais(requester, ai_defs.ai_prefix):
            print('Deleting AI {0}'.format(aiid))
            response = de_rate_limit(hu_api.api.delete_ai, requester, aiid)
            if not response.success:
                print('Failed to delete: {0}'.format(response.text))

    def create_upload_ai(size, tag, filename, ai_prefix):
        name = "{0}-{1}".format(ai_prefix, str(tag))

        print('Creating {0} AI '.format(name))
        creation = de_rate_limit(hu_api.api.create_ai, requester, name, "Testing with {0} lines of training data".format(size))
        if not creation.success:
            print("Error creating AI: {0}".format(creation.text))
        aiid = creation.response["aiid"]

        print('Uploading new data file')
        upload = de_rate_limit(hu_api.api.upload_training, requester, aiid, filename)
        if not upload.success:
            print("Error uploading training data: {0}".format(upload.text))

        return aiid


    def train_start_ai(aiid):

        print('Starting training')

        while True:
            status_request = de_rate_limit(hu_api.api.get_ai, requester, aiid)
            if status_request.response["ai_status"] == "ai_ready_to_train":
                break
            else:
                print("Waiting for status to flip ...")
                time.sleep(1)

        train = de_rate_limit(hu_api.api.start_training, requester, aiid)
        if not train.success:
            print("Error starting training: {0}".format(train.text))

    def create_sentence(source_word_set, common_words):
        big_word_count = random.randint(1, 3)
        small_word_count = 2 * big_word_count + random.randint(-1, 3)
        using_big_words = random.sample(source_word_set, big_word_count)
        source_word_set -= set(using_big_words)
        question = using_big_words + random.sample(common_words, small_word_count)
        random.shuffle(question)
        return ' '.join(question)

    def load_word_list():
        # load the full word list
        filtered = set([line.strip() for line in read_lines("wordlist.txt")
                        if "'" not in line and line == line.lower()])

        # remove common words
        for word in config.common_words:
            filtered.discard(word)

        print('Unique words ', len(filtered))
        return filtered

    def create_test_bot_lines(wordset, line_count):
        training_data = []
        for a in range(line_count // 2):
            question = create_sentence(wordset, config.common_words)
            answer = create_sentence(wordset, config.common_words)
            training_data.append((question, answer))
        return training_data

    def create_new_ai_files(ai_defs):
        filtered = load_word_list()
        for size, tag, filename in ai_defs.defs:
            create_new_ai_pairs(filtered, size, filename)

    def create_new_ai_pairs(word_set, line_count, filename):
        training_data = create_test_bot_lines(word_set, line_count);
        print("Generated {0} pairs, {1} words left over: into {2}"
              .format(len(training_data), len(word_set), filename))

        text = []
        for q, a in training_data:
            text.append(q)
            text.append(a)
            text.append('')

        write_file_lines(filename, text)

    def create_and_train_new_ais(ai_defs):
        aiids = []
        for size, tag, filename in ai_defs.defs:
            aiids.append(create_upload_ai(size, tag, filename, ai_defs.ai_prefix))
        for aiid in aiids:
            train_start_ai(aiid)

    def have_we_got_the_files(ai_defs):
        missing_files = False
        for size, tag, filename in ai_defs.defs:
            if not Path(filename).is_file():
                missing_files = True
                print("Missing {0}; recreating all data files".format(filename))
        return not missing_files

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    load_test_defs = config.load_test_ais
    chat_test_defs = config.chat_test_ais

    # if files are missing then recreate from scratch
    if not have_we_got_the_files(load_test_defs):
        create_new_ai_files(load_test_defs)
    if not have_we_got_the_files(chat_test_defs):
        create_new_ai_files(chat_test_defs)

    # delete all the old load test AIs
    delete_test_ais(load_test_defs)
    delete_test_ais(chat_test_defs)

    # upload new training data
    create_and_train_new_ais(load_test_defs)
    create_and_train_new_ais(chat_test_defs)


if __name__ == "__main__":
    config = make_config()
    main()